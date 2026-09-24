package com.wr.nutmeg.league;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.league.dtos.LeagueTableResponse;
import com.wr.nutmeg.match.Fixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeagueStandingServiceTest {

    @Mock
    private LeagueRepository leagueRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private LeagueStandingRepository standingRepository;

    @InjectMocks
    private LeagueStandingService leagueStandingService;

    private League league;
    private Club home;
    private Club away;

    @BeforeEach
    void setUp() {
        league = new League();
        league.setId(UUID.randomUUID());
        league.setName("Premier Division");
        league.setCurrentRound(2);

        home = club("Alpha FC");
        away = club("Beta United");
    }

    @Test
    void applyFixtureResultAwardsWinAndLoss() {
        Fixture fixture = fixture(2, 0);
        LeagueStanding homeStanding = LeagueStanding.zeroed(league, home);
        LeagueStanding awayStanding = LeagueStanding.zeroed(league, away);

        when(standingRepository.findByLeagueIdAndClubId(league.getId(), home.getId()))
                .thenReturn(Optional.of(homeStanding));
        when(standingRepository.findByLeagueIdAndClubId(league.getId(), away.getId()))
                .thenReturn(Optional.of(awayStanding));
        when(standingRepository.save(any(LeagueStanding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        leagueStandingService.applyFixtureResult(fixture);

        assertThat(homeStanding.getWins()).isEqualTo(1);
        assertThat(homeStanding.getPoints()).isEqualTo(3);
        assertThat(homeStanding.getGoalsFor()).isEqualTo(2);
        assertThat(homeStanding.getGoalsAgainst()).isEqualTo(0);
        assertThat(awayStanding.getLosses()).isEqualTo(1);
        assertThat(awayStanding.getPoints()).isEqualTo(0);
        assertThat(awayStanding.getGoalsFor()).isEqualTo(0);
        assertThat(awayStanding.getGoalsAgainst()).isEqualTo(2);
    }

    @Test
    void getTableSortsByPointsThenGoalDifferenceThenGoalsFor() {
        Club gamma = club("Gamma City");
        LeagueStanding first = standing(gamma, 3, 2, 0, 1, 8, 4, 6);
        LeagueStanding second = standing(home, 3, 2, 0, 1, 6, 2, 6);
        LeagueStanding third = standing(away, 3, 1, 1, 1, 4, 4, 4);

        when(leagueRepository.findById(league.getId())).thenReturn(Optional.of(league));
        when(clubRepository.findByLeagueId(league.getId())).thenReturn(List.of(home, away, gamma));
        when(standingRepository.findByLeagueIdWithClub(league.getId()))
                .thenReturn(List.of(third, second, first));

        LeagueTableResponse table = leagueStandingService.getTable(league.getId());

        assertThat(table.leagueId()).isEqualTo(league.getId());
        assertThat(table.standings()).hasSize(3);
        assertThat(table.standings().get(0).clubName()).isEqualTo("Gamma City");
        assertThat(table.standings().get(0).position()).isEqualTo(1);
        assertThat(table.standings().get(1).clubName()).isEqualTo("Alpha FC");
        assertThat(table.standings().get(1).goalDifference()).isEqualTo(4);
        assertThat(table.standings().get(2).clubName()).isEqualTo("Beta United");
        assertThat(table.standings().get(2).points()).isEqualTo(4);
    }

    @Test
    void getTableThrowsWhenLeagueMissing() {
        UUID missingId = UUID.randomUUID();
        when(leagueRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leagueStandingService.getTable(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    private Fixture fixture(int homeScore, int awayScore) {
        Fixture fixture = new Fixture();
        fixture.setLeague(league);
        fixture.setHomeClub(home);
        fixture.setAwayClub(away);
        fixture.setHomeScore(homeScore);
        fixture.setAwayScore(awayScore);
        return fixture;
    }

    private Club club(String name) {
        Club club = new Club();
        club.setId(UUID.randomUUID());
        club.setName(name);
        return club;
    }

    private LeagueStanding standing(
            Club club,
            int played,
            int wins,
            int draws,
            int losses,
            int goalsFor,
            int goalsAgainst,
            int points
    ) {
        LeagueStanding standing = LeagueStanding.zeroed(league, club);
        standing.setPlayed(played);
        standing.setWins(wins);
        standing.setDraws(draws);
        standing.setLosses(losses);
        standing.setGoalsFor(goalsFor);
        standing.setGoalsAgainst(goalsAgainst);
        standing.setPoints(points);
        return standing;
    }
}
