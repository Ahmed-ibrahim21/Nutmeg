package com.wr.nutmeg.league;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.league.dtos.LeagueTableResponse;
import com.wr.nutmeg.league.dtos.LeagueTableRowResponse;
import com.wr.nutmeg.match.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LeagueStandingService {

    private static final Comparator<LeagueStanding> TABLE_ORDER = Comparator
            .comparingInt(LeagueStanding::getPoints)
            .thenComparingInt(LeagueStanding::goalDifference)
            .thenComparingInt(LeagueStanding::getGoalsFor)
            .reversed()
            .thenComparing(standing -> standing.getClub().getName(), String.CASE_INSENSITIVE_ORDER);

    private final LeagueRepository leagueRepository;
    private final ClubRepository clubRepository;
    private final LeagueStandingRepository standingRepository;

    public LeagueStandingService(
            LeagueRepository leagueRepository,
            ClubRepository clubRepository,
            LeagueStandingRepository standingRepository
    ) {
        this.leagueRepository = leagueRepository;
        this.clubRepository = clubRepository;
        this.standingRepository = standingRepository;
    }

    @Transactional
    public void initializeStandings(League league, List<Club> clubs) {
        for (Club club : clubs) {
            getOrCreateStanding(league, club);
        }
    }

    @Transactional
    public void applyFixtureResult(Fixture fixture) {
        League league = fixture.getLeague();
        LeagueStanding home = getOrCreateStanding(league, fixture.getHomeClub());
        LeagueStanding away = getOrCreateStanding(league, fixture.getAwayClub());
        home.recordResult(fixture.getHomeScore(), fixture.getAwayScore());
        away.recordResult(fixture.getAwayScore(), fixture.getHomeScore());
        standingRepository.save(home);
        standingRepository.save(away);
    }

    @Transactional
    public LeagueTableResponse getTable(UUID leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found: " + leagueId));

        List<Club> clubs = clubRepository.findByLeagueId(leagueId);
        Map<UUID, LeagueStanding> byClubId = standingRepository.findByLeagueIdWithClub(leagueId).stream()
                .collect(Collectors.toMap(standing -> standing.getClub().getId(), Function.identity()));

        List<LeagueStanding> standings = new ArrayList<>();
        for (Club club : clubs) {
            LeagueStanding standing = byClubId.get(club.getId());
            if (standing == null) {
                standing = standingRepository.save(LeagueStanding.zeroed(league, club));
            }
            standings.add(standing);
        }

        standings.sort(TABLE_ORDER);

        List<LeagueTableRowResponse> rows = new ArrayList<>(standings.size());
        for (int i = 0; i < standings.size(); i++) {
            LeagueStanding standing = standings.get(i);
            rows.add(new LeagueTableRowResponse(
                    i + 1,
                    standing.getClub().getId(),
                    standing.getClub().getName(),
                    standing.getPlayed(),
                    standing.getWins(),
                    standing.getDraws(),
                    standing.getLosses(),
                    standing.getGoalsFor(),
                    standing.getGoalsAgainst(),
                    standing.goalDifference(),
                    standing.getPoints()
            ));
        }

        return new LeagueTableResponse(league.getId(), league.getName(), league.getCurrentRound(), rows);
    }

    private LeagueStanding getOrCreateStanding(League league, Club club) {
        return standingRepository.findByLeagueIdAndClubId(league.getId(), club.getId())
                .orElseGet(() -> standingRepository.save(LeagueStanding.zeroed(league, club)));
    }
}
