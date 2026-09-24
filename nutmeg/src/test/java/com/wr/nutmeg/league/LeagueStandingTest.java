package com.wr.nutmeg.league;

import com.wr.nutmeg.club.Club;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeagueStandingTest {

    @Test
    void recordsWinDrawAndLoss() {
        LeagueStanding standing = LeagueStanding.zeroed(new League(), new Club());

        standing.recordResult(2, 1);
        standing.recordResult(1, 1);
        standing.recordResult(0, 3);

        assertThat(standing.getPlayed()).isEqualTo(3);
        assertThat(standing.getWins()).isEqualTo(1);
        assertThat(standing.getDraws()).isEqualTo(1);
        assertThat(standing.getLosses()).isEqualTo(1);
        assertThat(standing.getGoalsFor()).isEqualTo(3);
        assertThat(standing.getGoalsAgainst()).isEqualTo(5);
        assertThat(standing.goalDifference()).isEqualTo(-2);
        assertThat(standing.getPoints()).isEqualTo(4);
    }
}
