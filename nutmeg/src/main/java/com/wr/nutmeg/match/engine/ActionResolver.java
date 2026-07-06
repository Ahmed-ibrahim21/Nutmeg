package com.wr.nutmeg.match.engine;

import com.wr.nutmeg.tactics.TacticsProfile;
import org.springframework.stereotype.Component;

@Component
public class ActionResolver {

    private static final double MIN_THRESHOLD = 15;
    private static final double MAX_THRESHOLD = 85;
    private static final double HOME_ADVANTAGE = 2;

    private static final double MIN_GOAL_THRESHOLD = 4;
    private static final double MAX_GOAL_THRESHOLD = 38;
    private static final double GOAL_BASE = 7;
    private static final double SHOOTING_WEIGHT = 0.15;
    private static final double GOAL_ATTACK_BIAS_WEIGHT = 0.15;
    private static final double GOAL_COHERENCE_WEIGHT = 3;
    private static final double KEEPER_DEFENDING_WEIGHT = 0.13;
    private static final double KEEPER_OVERALL_WEIGHT = 0.05;

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

        double threshold = GOAL_BASE;
        threshold += shooter.shooting() * SHOOTING_WEIGHT;
        threshold += attackTactics.attackBias() * GOAL_ATTACK_BIAS_WEIGHT;
        threshold += attacking.matchup().attackBias() * 0.10;
        threshold += attackTactics.coherenceScore() * GOAL_COHERENCE_WEIGHT;

        if (attacking.homeTeam()) {
            threshold += HOME_ADVANTAGE;
        }

        threshold -= keeper.defending() * KEEPER_DEFENDING_WEIGHT;
        threshold -= keeper.overall() * KEEPER_OVERALL_WEIGHT;
        threshold -= defending.tactics().defenseBias() * 0.15;
        threshold -= defending.matchup().defenseBias() * 0.10;

        return clampGoal(threshold);
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

    private double clampGoal(double value) {
        return Math.max(MIN_GOAL_THRESHOLD, Math.min(MAX_GOAL_THRESHOLD, value));
    }
}
