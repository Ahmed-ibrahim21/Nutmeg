package com.wr.nutmeg.tactics;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubLineup;
import com.wr.nutmeg.club.ClubLineupRepository;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.match.setup.MatchSetupService;
import com.wr.nutmeg.tactics.dtos.SetTacticsRequest;
import com.wr.nutmeg.tactics.dtos.TacticsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TacticsService {

    private final ClubRepository clubRepository;
    private final ClubLineupRepository clubLineupRepository;
    private final MatchSetupService matchSetupService;
    private final TacticsCoherenceValidator tacticsCoherenceValidator;

    public TacticsService(
            ClubRepository clubRepository,
            ClubLineupRepository clubLineupRepository,
            MatchSetupService matchSetupService,
            TacticsCoherenceValidator tacticsCoherenceValidator
    ) {
        this.clubRepository = clubRepository;
        this.clubLineupRepository = clubLineupRepository;
        this.matchSetupService = matchSetupService;
        this.tacticsCoherenceValidator = tacticsCoherenceValidator;
    }

    @Transactional(readOnly = true)
    public TacticsResponse getTactics(UUID clubId) {
        Club club = loadClub(clubId);
        ClubLineup lineup = clubLineupRepository.findByClubId(clubId)
                .orElseGet(() -> matchSetupService.getOrCreateLineup(club, Formation.F_4_4_2));

        MatchTactics tactics = lineup.getTactics();
        double coherenceScore = tacticsCoherenceValidator.buildProfile(tactics).coherenceScore();
        return TacticsResponse.from(tactics, coherenceScore);
    }

    @Transactional
    public TacticsResponse updateTactics(UUID clubId, SetTacticsRequest request) {
        Club club = loadClub(clubId);
        ClubLineup lineup = clubLineupRepository.findByClubId(clubId)
                .orElseGet(() -> matchSetupService.getOrCreateLineup(club, Formation.F_4_4_2));

        Formation previousFormation = lineup.getTactics().getFormation();
        applyRequest(lineup.getTactics(), request);

        if (previousFormation != request.formation()) {
            lineup = matchSetupService.rebuildLineupForFormation(lineup, request.formation());
        }

        clubLineupRepository.save(lineup);

        double coherenceScore = tacticsCoherenceValidator.buildProfile(lineup.getTactics()).coherenceScore();
        return TacticsResponse.from(lineup.getTactics(), coherenceScore);
    }

    private Club loadClub(UUID clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
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

