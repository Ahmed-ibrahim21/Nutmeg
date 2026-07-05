package com.wr.nutmeg.match.engine;

import com.wr.nutmeg.common.enums.MatchEvents;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchSimulator {

    private static final int POSSESSIONS_PER_MATCH = 90;
    private static final int MAX_ACTIONS_PER_POSSESSION = 4;

    private final ActionPicker actionPicker;
    private final ActionResolver actionResolver;

    public MatchSimulator(ActionPicker actionPicker, ActionResolver actionResolver) {
        this.actionPicker = actionPicker;
        this.actionResolver = actionResolver;
    }

    public MatchResult simulate(TeamState home, TeamState away, long seed) {
        MatchContext context = new MatchContext(home, away, seed);

        for (int i = 0; i < POSSESSIONS_PER_MATCH && context.minute() < 90; i++) {
            context.recordPossessionStart();
            simulatePossession(context);
        }

        return new MatchResult(
                context.homeScore(),
                context.awayScore(),
                context.homePossessions(),
                context.awayPossessions(),
                List.copyOf(context.events())
        );
    }

    private void simulatePossession(MatchContext context) {
        context.advanceMinute(context.random().nextInt(1, 3));

        for (int actionIndex = 0; actionIndex < MAX_ACTIONS_PER_POSSESSION; actionIndex++) {
            ActionType action = actionPicker.pickAction(context);

            if (action == ActionType.SHOOT || shouldForceShot(context, action)) {
                resolveShot(context);
                switchPossession(context);
                return;
            }

            double threshold = actionResolver.calculateThreshold(action, context);
            if (actionResolver.rollSuccess(threshold, context)) {
                advanceAfterSuccess(context, action);
                if (context.zone() == PitchZone.ATTACKING_THIRD && context.random().nextInt(100) < 35) {
                    resolveShot(context);
                    switchPossession(context);
                    return;
                }
            } else {
                context.addEvent(new SimulatedEvent(
                        context.minute(),
                        MatchEvents.TURNOVER,
                        context.possession(),
                        context.ballCarrier().id(),
                        context.ballCarrier().name(),
                        null,
                        null,
                        action.name()
                ));
                switchPossession(context);
                return;
            }
        }

        switchPossession(context);
    }

    private boolean shouldForceShot(MatchContext context, ActionType action) {
        return context.zone() == PitchZone.ATTACKING_THIRD
                && action == ActionType.DRIBBLE
                && context.random().nextInt(100) < 20;
    }

    private void advanceAfterSuccess(MatchContext context, ActionType action) {
        PitchZone nextZone = switch (context.zone()) {
            case DEFENSIVE_THIRD -> action == ActionType.CLEARANCE ? PitchZone.MIDFIELD : PitchZone.MIDFIELD;
            case MIDFIELD -> PitchZone.ATTACKING_THIRD;
            case ATTACKING_THIRD -> PitchZone.ATTACKING_THIRD;
        };
        context.setZone(nextZone);
        context.setBallCarrier(context.pickCarrier(context.teamInPossession(), nextZone));
    }

    private void resolveShot(MatchContext context) {
        context.setBallCarrier(context.pickCarrier(context.teamInPossession(), PitchZone.ATTACKING_THIRD));
        double shotThreshold = actionResolver.calculateShotThreshold(context);

        if (actionResolver.rollGoal(shotThreshold, context)) {
            context.score(context.possession());
            context.addEvent(new SimulatedEvent(
                    context.minute(),
                    MatchEvents.GOAL,
                    context.possession(),
                    context.ballCarrier().id(),
                    context.ballCarrier().name(),
                    null,
                    null,
                    "Goal"
            ));
            return;
        }

        if (actionResolver.rollChanceAfterShot(shotThreshold, context)) {
            MatchEvents eventType = context.random().nextBoolean() ? MatchEvents.SAVE : MatchEvents.CHANCE_MISSED;
            context.addEvent(new SimulatedEvent(
                    context.minute(),
                    eventType,
                    context.possession(),
                    context.ballCarrier().id(),
                    context.ballCarrier().name(),
                    context.defendingTeam().goalkeeper().id(),
                    context.defendingTeam().goalkeeper().name(),
                    eventType.name()
            ));
            return;
        }

        context.addEvent(new SimulatedEvent(
                context.minute(),
                MatchEvents.CHANCE_MISSED,
                context.possession(),
                context.ballCarrier().id(),
                context.ballCarrier().name(),
                null,
                null,
                "Shot off target"
        ));
    }

    private void switchPossession(MatchContext context) {
        TeamSide next = context.possession().opposite();
        context.setPossession(next);
        context.setZone(PitchZone.MIDFIELD);
        context.setBallCarrier(context.pickCarrier(context.teamInPossession(), PitchZone.MIDFIELD));
    }
}
