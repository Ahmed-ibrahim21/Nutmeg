package com.wr.nutmeg.match.engine;

import com.wr.nutmeg.match.tactics.TacticsProfile;
import org.springframework.stereotype.Component;

@Component
public class ActionResolver {

    private static final double MIN_THRESHOLD = 15;
    private static final double MAX_THRESHOLD = 85;
    private static final double HOME_ADVANTAGE = 4;

    public double calculateThreshold(ActionType action, MatchContext context) {
        TeamState attacking = context.teamInPossession();
        TeamState defending = context.defendingTeam();
        PlayerState carrier = context.ballCarrier();
        TacticsProfile attackTactics = attacking.tactics();
        TacticsProfile defendTactics = defending.tactics();

        double threshold = 50;
        threshold += playerSkill(action, carrier) * 0.25;
        threshold += teamSupport(action, attacking) * 0.10;
        threshold += attackTactics.possessionBias() * 0.15;
        threshold += attackTactics.coherenceScore() * 8;
        threshold += attacking.matchup().possessionBias();
        threshold += attacking.matchup().attackBias() * actionAttackWeight(action);

        if (attacking.homeTeam()) {
            threshold += HOME_ADVANTAGE;
        }

        threshold += fitnessModifier(carrier);
        threshold -= defendTactics.pressIntensity() * 0.35;
        threshold -= defendTactics.defenseBias() * 0.25;
        threshold -= defending.matchup().defenseBias() * 0.20;
        threshold -= defending.lineOverall(com.wr.nutmeg.common.enums.PlayerRole.DEF) * 0.08;

        if (action == ActionType.SHOOT) {
            threshold += attackTactics.attackBias() * 0.35;
        }

        return clamp(threshold);
    }

    public double calculateShotThreshold(MatchContext context) {
        TeamState attacking = context.teamInPossession();
        TeamState defending = context.defendingTeam();
        PlayerState shooter = context.ballCarrier();
        PlayerState keeper = defending.goalkeeper();
        TacticsProfile attackTactics = attacking.tactics();

        double threshold = 28;
        threshold += shooter.shooting() * 0.28;
        threshold += attackTactics.attackBias() * 0.30;
        threshold += attacking.matchup().attackBias() * 0.20;
        threshold += attackTactics.coherenceScore() * 6;

        if (attacking.homeTeam()) {
            threshold += HOME_ADVANTAGE;
        }

        threshold -= keeper.defending() * 0.22;
        threshold -= keeper.overall() * 0.08;
        threshold -= defending.tactics().defenseBias() * 0.25;
        threshold -= defending.matchup().defenseBias() * 0.15;

        return clamp(threshold);
    }

    public boolean rollSuccess(double threshold, MatchContext context) {
        int roll = context.random().nextInt(1, 101);
        return roll <= threshold;
    }

    public boolean rollGoal(double shotThreshold, MatchContext context) {
        int roll = context.random().nextInt(1, 101);
        int luck = context.random().nextInt(-3, 4);
        return roll <= shotThreshold + luck;
    }

    public boolean rollChanceAfterShot(double shotThreshold, MatchContext context) {
        int roll = context.random().nextInt(1, 101);
        return roll <= shotThreshold + 15;
    }

    private double playerSkill(ActionType action, PlayerState carrier) {
        return switch (action) {
            case SHORT_PASS, CROSS, LONG_BALL -> carrier.passing();
            case DRIBBLE -> average(carrier.dribbling(), carrier.pace());
            case SHOOT -> carrier.shooting();
            case CLEARANCE -> average(carrier.defending(), carrier.physical());
        };
    }

    private double teamSupport(ActionType action, TeamState team) {
        return switch (action) {
            case SHORT_PASS, LONG_BALL, CROSS -> team.lineOverall(com.wr.nutmeg.common.enums.PlayerRole.MID);
            case DRIBBLE, SHOOT -> team.lineOverall(com.wr.nutmeg.common.enums.PlayerRole.FWD);
            case CLEARANCE -> team.lineOverall(com.wr.nutmeg.common.enums.PlayerRole.DEF);
        };
    }

    private double actionAttackWeight(ActionType action) {
        return action == ActionType.SHOOT ? 0.35 : 0.10;
    }

    private double fitnessModifier(PlayerState carrier) {
        return (carrier.fitness() - 100) * 0.08;
    }

    private double average(int first, int second) {
        return (first + second) / 2.0;
    }

    private double clamp(double value) {
        return Math.max(MIN_THRESHOLD, Math.min(MAX_THRESHOLD, value));
    }
}
