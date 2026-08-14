package com.wr.nutmeg.tactics;

import com.wr.nutmeg.club.ClubLineup;
import com.wr.nutmeg.club.ClubLineupRepository;
import com.wr.nutmeg.exceptions.InvlaidStateException;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.manager.Manager;
import com.wr.nutmeg.manager.ManagerRepository;
import com.wr.nutmeg.match.setup.MatchSetupService;
import com.wr.nutmeg.tactics.dtos.SetTacticsRequest;
import com.wr.nutmeg.tactics.dtos.TacticsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TacticsService {

    private final ManagerRepository managerRepository;
    private final ClubLineupRepository clubLineupRepository;
    private final MatchSetupService matchSetupService;
    private final TacticsCoherenceValidator tacticsCoherenceValidator;

    public TacticsService(
            ManagerRepository managerRepository,
            ClubLineupRepository clubLineupRepository,
            MatchSetupService matchSetupService,
            TacticsCoherenceValidator tacticsCoherenceValidator
    ) {
        this.managerRepository = managerRepository;
        this.clubLineupRepository = clubLineupRepository;
        this.matchSetupService = matchSetupService;
        this.tacticsCoherenceValidator = tacticsCoherenceValidator;
    }

    @Transactional(readOnly = true)
    public TacticsResponse getTactics(UUID managerId) {
        Manager manager = loadManagerWithClub(managerId);
        ClubLineup lineup = clubLineupRepository.findByClubId(manager.getClub().getId())
                .orElseGet(() -> matchSetupService.getOrCreateLineup(manager.getClub(), Formation.F_4_4_2));

        MatchTactics tactics = lineup.getTactics();
        double coherenceScore = tacticsCoherenceValidator.buildProfile(tactics).coherenceScore();
        return TacticsResponse.from(tactics, coherenceScore);
    }

    @Transactional
    public TacticsResponse updateTactics(UUID managerId, SetTacticsRequest request) {
        Manager manager = loadManagerWithClub(managerId);
        ClubLineup lineup = clubLineupRepository.findByClubId(manager.getClub().getId())
                .orElseGet(() -> matchSetupService.getOrCreateLineup(manager.getClub(), Formation.F_4_4_2));

        Formation previousFormation = lineup.getTactics().getFormation();
        applyRequest(lineup.getTactics(), request);

        if (previousFormation != request.formation()) {
            lineup = matchSetupService.rebuildLineupForFormation(lineup, request.formation());
        }

        clubLineupRepository.save(lineup);

        double coherenceScore = tacticsCoherenceValidator.buildProfile(lineup.getTactics()).coherenceScore();
        return TacticsResponse.from(lineup.getTactics(), coherenceScore);
    }

    private Manager loadManagerWithClub(UUID managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + managerId));
        if (manager.getClub() == null) {
            throw new InvlaidStateException("Manager does not manage any club yet");
        }
        return manager;
    }

    private void applyRequest(MatchTactics tactics, SetTacticsRequest request) {
        tactics.setFormation(request.formation());
        tactics.setGamePlan(request.gamePlan());
        tactics.setForwardLine(request.forwardLine());
        tactics.setMidfieldLine(request.midfieldLine());
        tactics.setDefenseLine(request.defenseLine());
        tactics.setPressing(request.pressing());
        tactics.setStyle(request.style());
        tactics.setTempo(request.tempo());
        tactics.setTackling(request.tackling());
        tactics.setMarking(request.marking());
        tactics.setOffsideTrap(request.offsideTrap());
    }
}
