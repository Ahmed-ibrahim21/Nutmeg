package com.wr.nutmeg.match.setup;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubLineup;
import com.wr.nutmeg.club.ClubLineupRepository;
import com.wr.nutmeg.common.enums.PlayerRole;
import com.wr.nutmeg.match.tactics.DefaultTacticsFactory;
import com.wr.nutmeg.match.tactics.Formation;
import com.wr.nutmeg.match.tactics.FormationSlot;
import com.wr.nutmeg.player.Player;
import com.wr.nutmeg.player.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Reads a club's standing lineup, auto-generating and persisting a default
 * one (best-XI by rating) the first time a club is asked for. Once a lineup
 * exists it belongs to the club, not to any particular fixture, so it is
 * simply reused for every match until it is next changed.
 */
@Service
public class MatchSetupService {

    private final ClubLineupRepository clubLineupRepository;
    private final PlayerRepository playerRepository;
    private final DefaultTacticsFactory defaultTacticsFactory;

    public MatchSetupService(
            ClubLineupRepository clubLineupRepository,
            PlayerRepository playerRepository,
            DefaultTacticsFactory defaultTacticsFactory
    ) {
        this.clubLineupRepository = clubLineupRepository;
        this.playerRepository = playerRepository;
        this.defaultTacticsFactory = defaultTacticsFactory;
    }

    @Transactional
    public ClubLineup getOrCreateLineup(Club club, Formation defaultFormation) {
        return clubLineupRepository.findByClubId(club.getId())
                .orElseGet(() -> clubLineupRepository.save(buildDefaultLineup(club, defaultFormation)));
    }

    private ClubLineup buildDefaultLineup(Club club, Formation formation) {
        List<Player> squad = playerRepository.findAll().stream()
                .filter(player -> player.getClub() != null && player.getClub().getId().equals(club.getId()))
                .filter(player -> !player.isSuspended() && !player.isInjured())
                .toList();

        ClubLineup clubLineup = new ClubLineup();
        clubLineup.setClub(club);
        clubLineup.setTactics(defaultTacticsFactory.forFormation(formation));
        clubLineup.setLineup(buildLineup(squad, formation));
        return clubLineup;
    }

    private List<LineupAssignment> buildLineup(List<Player> squad, Formation formation) {
        List<FormationSlot> slots = FormationTemplate.slotsFor(formation);
        Map<PlayerRole, List<Player>> byRole = new EnumMap<>(PlayerRole.class);
        for (PlayerRole role : PlayerRole.values()) {
            byRole.put(role, new ArrayList<>());
        }
        for (Player player : squad) {
            byRole.get(player.getPosition().getRole()).add(player);
        }
        for (List<Player> players : byRole.values()) {
            players.sort(Comparator.comparingInt(Player::getOverallRating).reversed());
        }

        Set<UUID> used = new HashSet<>();
        List<LineupAssignment> assignments = new ArrayList<>();
        for (FormationSlot slot : slots) {
            Player selected = byRole.get(slot.getRole()).stream()
                    .filter(player -> !used.contains(player.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Not enough players for slot " + slot + " in formation " + formation
                    ));
            used.add(selected.getId());
            LineupAssignment assignment = new LineupAssignment();
            assignment.setSlot(slot);
            assignment.setPlayerId(selected.getId());
            assignments.add(assignment);
        }
        return assignments;
    }
}