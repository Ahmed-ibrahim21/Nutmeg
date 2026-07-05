package com.wr.nutmeg.match.engine;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.match.tactics.MatchupModifiers;
import com.wr.nutmeg.match.tactics.TacticsProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ActionResolverTest {

    private ActionResolver actionResolver;

    @BeforeEach
    void setUp() {
        actionResolver = new ActionResolver();
    }

    @Test
    void betterPlayerGetsHigherPassThreshold() {
        MatchContext weakContext = buildContext(player(60, 60), player(70, 70), TeamSide.HOME);
        MatchContext strongContext = buildContext(player(85, 85), player(70, 70), TeamSide.HOME);

        double weakThreshold = actionResolver.calculateThreshold(ActionType.SHORT_PASS, weakContext);
        double strongThreshold = actionResolver.calculateThreshold(ActionType.SHORT_PASS, strongContext);

        assertThat(strongThreshold).isGreaterThan(weakThreshold);
    }

    @Test
    void homeTeamGetsSmallAdvantage() {
        MatchContext homeContext = buildContext(player(70, 70), player(70, 70), TeamSide.HOME);
        MatchContext awayContext = buildContext(player(70, 70), player(70, 70), TeamSide.AWAY);

        double homeThreshold = actionResolver.calculateThreshold(ActionType.SHORT_PASS, homeContext);
        double awayThreshold = actionResolver.calculateThreshold(ActionType.SHORT_PASS, awayContext);

        assertThat(homeThreshold).isGreaterThan(awayThreshold);
    }

    @Test
    void thresholdsStayWithinBounds() {
        MatchContext context = buildContext(player(99, 99), player(40, 40), TeamSide.HOME);

        double threshold = actionResolver.calculateThreshold(ActionType.SHOOT, context);

        assertThat(threshold).isBetween(15.0, 85.0);
    }

    private MatchContext buildContext(PlayerState carrier, PlayerState defender, TeamSide possession) {
        TeamState home = team(true, carrier, defender, possession == TeamSide.HOME);
        TeamState away = team(false, defender, carrier, possession == TeamSide.AWAY);
        MatchContext context = new MatchContext(home, away, 42L);
        context.setPossession(possession);
        context.setBallCarrier(carrier);
        context.setZone(PitchZone.MIDFIELD);
        return context;
    }

    private TeamState team(boolean home, PlayerState primary, PlayerState secondary, boolean primaryPossession) {
        PlayerState gk = new PlayerState(UUID.randomUUID(), "Keeper", com.wr.nutmeg.common.enums.PlayerRole.GK,
                70, 50, 50, 50, 75, 70, 50, 100);
        List<PlayerState> lineup = List.of(gk, primary, secondary);
        TacticsProfile profile = TacticsProfile.neutral();
        Club club = new Club();
        club.setName(home ? "Home FC" : "Away FC");
        return new TeamState(club, home, profile, MatchupModifiers.none(), Map.of(), lineup);
    }

    private PlayerState player(int passing, int overall) {
        return new PlayerState(
                UUID.randomUUID(),
                "Player",
                com.wr.nutmeg.common.enums.PlayerRole.MID,
                overall,
                passing,
                60,
                60,
                55,
                60,
                60,
                100
        );
    }
}
