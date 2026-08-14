package com.wr.nutmeg.club;

import com.wr.nutmeg.club.dtos.LineupAssignmentDto;
import com.wr.nutmeg.club.dtos.LineupPlayerResponse;
import com.wr.nutmeg.club.dtos.LineupResponse;
import com.wr.nutmeg.club.dtos.SetLineupRequest;
import com.wr.nutmeg.exceptions.InvalidArgumentsException;
import com.wr.nutmeg.exceptions.InvlaidStateException;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.manager.Manager;
import com.wr.nutmeg.manager.ManagerRepository;
import com.wr.nutmeg.match.setup.FormationTemplate;
import com.wr.nutmeg.match.setup.LineupAssignment;
import com.wr.nutmeg.match.setup.MatchSetupService;
import com.wr.nutmeg.player.Player;
import com.wr.nutmeg.player.PlayerRepository;
import com.wr.nutmeg.tactics.Formation;
import com.wr.nutmeg.tactics.FormationSlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LineupService {

    private final ManagerRepository managerRepository;
    private final ClubLineupRepository clubLineupRepository;
    private final PlayerRepository playerRepository;
    private final MatchSetupService matchSetupService;

    public LineupService(
            ManagerRepository managerRepository,
            ClubLineupRepository clubLineupRepository,
            PlayerRepository playerRepository,
            MatchSetupService matchSetupService
    ) {
        this.managerRepository = managerRepository;
        this.clubLineupRepository = clubLineupRepository;
        this.playerRepository = playerRepository;
        this.matchSetupService = matchSetupService;
    }

    @Transactional(readOnly = true)
    public LineupResponse getLineup(UUID managerId) {
        Manager manager = loadManagerWithClub(managerId);
        ClubLineup clubLineup = clubLineupRepository.findByClubId(manager.getClub().getId())
                .orElseGet(() -> matchSetupService.getOrCreateLineup(manager.getClub(), Formation.F_4_4_2));

        return toResponse(clubLineup);
    }

    @Transactional
    public LineupResponse updateLineup(UUID managerId, SetLineupRequest request) {
        Manager manager = loadManagerWithClub(managerId);
        ClubLineup clubLineup = clubLineupRepository.findByClubId(manager.getClub().getId())
                .orElseGet(() -> matchSetupService.getOrCreateLineup(manager.getClub(), Formation.F_4_4_2));

        Formation currentFormation = clubLineup.getTactics().getFormation();
        validateLineup(request.lineup(), currentFormation, manager.getClub().getId());

        clubLineup.getLineup().clear();
        for (LineupAssignmentDto dto : request.lineup()) {
            LineupAssignment assignment = new LineupAssignment();
            assignment.setSlot(dto.slot());
            assignment.setPlayerId(dto.playerId());
            clubLineup.getLineup().add(assignment);
        }

        clubLineupRepository.save(clubLineup);
        return toResponse(clubLineup);
    }

    private void validateLineup(List<LineupAssignmentDto> assignments, Formation formation, UUID clubId) {
        List<FormationSlot> expectedSlots = FormationTemplate.slotsFor(formation);

        // Verify the submitted slots match the current formation exactly
        Set<FormationSlot> submittedSlots = assignments.stream()
                .map(LineupAssignmentDto::slot)
                .collect(Collectors.toSet());

        if (submittedSlots.size() != assignments.size()) {
            throw new InvalidArgumentsException("Duplicate slots in lineup");
        }

        Set<FormationSlot> expectedSlotSet = new HashSet<>(expectedSlots);
        if (!submittedSlots.equals(expectedSlotSet)) {
            throw new InvalidArgumentsException(
                    "Lineup slots do not match the current formation " + formation
            );
        }

        // Verify no duplicate players
        Set<UUID> playerIds = assignments.stream()
                .map(LineupAssignmentDto::playerId)
                .collect(Collectors.toSet());

        if (playerIds.size() != assignments.size()) {
            throw new InvalidArgumentsException("A player cannot be assigned to multiple slots");
        }

        // Verify all players belong to this club and are available
        List<Player> players = playerRepository.findAllById(playerIds);
        if (players.size() != playerIds.size()) {
            throw new ResourceNotFoundException("One or more players not found");
        }

        for (Player player : players) {
            if (player.getClub() == null || !player.getClub().getId().equals(clubId)) {
                throw new InvalidArgumentsException("Player " + player.getFullName() + " does not belong to your club");
            }
            if (player.isSuspended()) {
                throw new InvalidArgumentsException("Player " + player.getFullName() + " is suspended");
            }
            if (player.isInjured()) {
                throw new InvalidArgumentsException("Player " + player.getFullName() + " is injured");
            }
        }
    }

    private LineupResponse toResponse(ClubLineup clubLineup) {
        List<UUID> playerIds = clubLineup.getLineup().stream()
                .map(LineupAssignment::getPlayerId)
                .toList();

        Map<UUID, Player> playersById = playerRepository.findAllById(playerIds).stream()
                .collect(Collectors.toMap(Player::getId, Function.identity()));

        List<LineupPlayerResponse> players = clubLineup.getLineup().stream()
                .map(assignment -> {
                    Player player = playersById.get(assignment.getPlayerId());
                    return new LineupPlayerResponse(
                            assignment.getSlot(),
                            player.getId(),
                            player.getFullName(),
                            player.getPosition(),
                            player.getOverallRating()
                    );
                })
                .toList();

        return new LineupResponse(clubLineup.getTactics().getFormation(), players);
    }

    private Manager loadManagerWithClub(UUID managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + managerId));
        if (manager.getClub() == null) {
            throw new InvlaidStateException("Manager does not manage any club yet");
        }
        return manager;
    }
}
