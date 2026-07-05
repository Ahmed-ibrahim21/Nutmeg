package com.wr.nutmeg.match.engine;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActionPicker {

    public ActionType pickAction(MatchContext context) {
        TacticsProfileHolder profile = TacticsProfileHolder.from(context.teamInPossession().tactics());

        if (context.zone() == PitchZone.ATTACKING_THIRD) {
            return weightedPick(context, shotHeavyWeights(profile));
        }
        if (context.zone() == PitchZone.DEFENSIVE_THIRD) {
            return weightedPick(context, defensiveThirdWeights(profile));
        }
        return weightedPick(context, midfieldWeights(profile));
    }

    private List<WeightedAction> shotHeavyWeights(TacticsProfileHolder profile) {
        List<WeightedAction> weights = new ArrayList<>();
        weights.add(new WeightedAction(ActionType.SHOOT, 35 + profile.attackBias()));
        weights.add(new WeightedAction(ActionType.DRIBBLE, 20 + profile.attackBias()));
        weights.add(new WeightedAction(ActionType.SHORT_PASS, 20));
        weights.add(new WeightedAction(ActionType.CROSS, 15 + profile.widthBias()));
        weights.add(new WeightedAction(ActionType.LONG_BALL, 5));
        return weights;
    }

    private List<WeightedAction> midfieldWeights(TacticsProfileHolder profile) {
        List<WeightedAction> weights = new ArrayList<>();
        weights.add(new WeightedAction(ActionType.SHORT_PASS, 40 + profile.possessionBias()));
        weights.add(new WeightedAction(ActionType.LONG_BALL, profile.longBallBias()));
        weights.add(new WeightedAction(ActionType.DRIBBLE, 15 + profile.attackBias()));
        weights.add(new WeightedAction(ActionType.CROSS, 10 + profile.widthBias()));
        weights.add(new WeightedAction(ActionType.CLEARANCE, 5));
        return weights;
    }

    private List<WeightedAction> defensiveThirdWeights(TacticsProfileHolder profile) {
        List<WeightedAction> weights = new ArrayList<>();
        weights.add(new WeightedAction(ActionType.CLEARANCE, 25 + profile.defenseBias()));
        weights.add(new WeightedAction(ActionType.LONG_BALL, 25 + profile.longBallBias()));
        weights.add(new WeightedAction(ActionType.SHORT_PASS, 30 + profile.possessionBias()));
        weights.add(new WeightedAction(ActionType.DRIBBLE, 10));
        weights.add(new WeightedAction(ActionType.CROSS, 5));
        return weights;
    }

    private ActionType weightedPick(MatchContext context, List<WeightedAction> weights) {
        int total = weights.stream().mapToInt(WeightedAction::weight).sum();
        int roll = context.random().nextInt(Math.max(total, 1));
        int cumulative = 0;
        for (WeightedAction weightedAction : weights) {
            cumulative += weightedAction.weight();
            if (roll < cumulative) {
                return weightedAction.action();
            }
        }
        return ActionType.SHORT_PASS;
    }

    private record WeightedAction(ActionType action, int weight) {
    }

    private record TacticsProfileHolder(
            int attackBias,
            int defenseBias,
            int possessionBias,
            int widthBias,
            int longBallBias
    ) {
        static TacticsProfileHolder from(com.wr.nutmeg.match.tactics.TacticsProfile profile) {
            return new TacticsProfileHolder(
                    (int) profile.attackBias(),
                    (int) profile.defenseBias(),
                    (int) profile.possessionBias(),
                    (int) profile.widthBias(),
                    profile.possessionBias() < 0 ? 15 : 5
            );
        }
    }
}
