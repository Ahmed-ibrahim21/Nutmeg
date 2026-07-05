package com.wr.nutmeg.match.engine;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.PlayerRole;
import com.wr.nutmeg.match.tactics.MatchupModifiers;
import com.wr.nutmeg.match.tactics.TacticsCoherenceValidator;
import com.wr.nutmeg.match.tactics.TacticsProfile;
import com.wr.nutmeg.match.tactics.MatchTactics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchSimulatorTest {

    private final MatchSimulator matchSimulator = new MatchSimulator(new ActionPicker(), new ActionResolver());

    @Test
    void simulateProducesReasonableScorelineWithFixedSeed() {
        MatchResult result = matchSimulator.simulate(strongTeam(true, 78), strongTeam(false, 72), 12345L);

        assertThat(result.homeScore()).isBetween(0, 8);
        assertThat(result.awayScore()).isBetween(0, 8);
        assertThat(result.homeScore() + result.awayScore()).isLessThanOrEqualTo(12);
        assertThat(result.events()).isNotEmpty();
    }

    @Test
    void strongerTeamWinsMoreOftenOverManySeeds() {
        int homeWins = 0;
        for (long seed = 1; seed <= 100; seed++) {
            MatchResult result = matchSimulator.simulate(
                    strongTeam(true, 82),
                    strongTeam(false, 65),
                    seed
            );
            if (result.homeScore() > result.awayScore()) {
                homeWins++;
            }
        }
        assertThat(homeWins).isGreaterThan(45);
    }

    private TeamState strongTeam(boolean home, int overall) {
        List<PlayerState> lineup = new ArrayList<>();
        lineup.add(state(PlayerRole.GK, overall - 5));
        for (int i = 0; i < 10; i++) {
            PlayerRole role = i < 4 ? PlayerRole.DEF : (i < 7 ? PlayerRole.MID : PlayerRole.FWD);
            lineup.add(state(role, overall));
        }

        MatchTactics tactics = new MatchTactics();
        TacticsProfile profile = new TacticsCoherenceValidator().buildProfile(tactics);
        Club club = new Club();
        club.setName(home ? "Home FC" : "Away FC");
        return new TeamState(club, home, profile, MatchupModifiers.none(), Map.of(), lineup);
    }

    private PlayerState state(PlayerRole role, int overall) {
        return new PlayerState(
                UUID.randomUUID(),
                role.name(),
                role,
                overall,
                overall,
                overall,
                overall,
                overall,
                overall,
                overall,
                100
        );
    }
}
