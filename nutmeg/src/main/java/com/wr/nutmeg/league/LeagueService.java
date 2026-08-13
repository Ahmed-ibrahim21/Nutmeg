package com.wr.nutmeg.league;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.common.enums.FixtureStatus;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.fixture.Fixture;
import com.wr.nutmeg.fixture.FixtureRepository;
import com.wr.nutmeg.league.dtos.FixtureResult;
import com.wr.nutmeg.league.dtos.RoundSimulationResult;
import com.wr.nutmeg.match.MatchSimulationService;
import com.wr.nutmeg.match.engine.MatchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LeagueService {

    private static final int PAGE_SIZE = 10;

    private final LeagueRepository leagueRepository;
    private final ClubRepository clubRepository;
    private final FixtureRepository fixtureRepository;
    private final MatchSimulationService matchSimulationService;

    public LeagueService(
            LeagueRepository leagueRepository,
            ClubRepository clubRepository,
            FixtureRepository fixtureRepository,
            MatchSimulationService matchSimulationService
    ) {
        this.leagueRepository = leagueRepository;
        this.clubRepository = clubRepository;
        this.fixtureRepository = fixtureRepository;
        this.matchSimulationService = matchSimulationService;
    }

    public Page<League> getPublicLeagues(int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by("name").ascending());
        return leagueRepository.findByLeagueVisibility(LeagueVisibility.PUBLIC, pageRequest);
    }

    public List<Club> getClubsByLeagueId(UUID leagueId) {
        if (!leagueRepository.existsById(leagueId)) {
            throw new ResourceNotFoundException("League not found: " + leagueId);
        }
        return clubRepository.findByLeagueId(leagueId);
    }

    @Transactional
    public RoundSimulationResult simulateCurrentRound(UUID leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found: " + leagueId));

        int round = league.getCurrentRound() + 1;

        List<Fixture> fixtures = fixtureRepository.findByLeagueIdAndRoundAndStatus(
                leagueId, round, FixtureStatus.SCHEDULED
        );

        if (fixtures.isEmpty()) {
            throw new IllegalStateException("No scheduled fixtures found for round " + round);
        }

        List<FixtureResult> results = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            MatchResult matchResult = matchSimulationService.simulateFixture(fixture.getId(), null);
            results.add(new FixtureResult(
                    fixture.getId(),
                    fixture.getHomeClub().getName(),
                    fixture.getAwayClub().getName(),
                    matchResult.homeScore(),
                    matchResult.awayScore()
            ));
        }

        league.setCurrentRound(round);
        leagueRepository.save(league);

        return new RoundSimulationResult(leagueId, round, results.size(), results);
    }


}
