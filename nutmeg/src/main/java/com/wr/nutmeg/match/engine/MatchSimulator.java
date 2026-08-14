package com.wr.nutmeg.match.engine;

import com.wr.nutmeg.common.enums.MatchEvents;
import com.wr.nutmeg.common.enums.PlayerRole;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchSimulator {

    private static final int POSSESSIONS_PER_MATCH = 90;
    private static final int MAX_ACTIONS_PER_POSSESSION = 4;
    private static final double CARD_BASE_CHANCE = 3.0;
    private static final double CARD_RISK_WEIGHT = 0.5;
    private static final double CARD_PHYSICAL_WEIGHT = 0.03;

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
                rollForCard(context);
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
            PlayerState scorer = context.ballCarrier();
            context.score(context.possession());
            context.addEvent(new SimulatedEvent(
                    context.minute(),
                    MatchEvents.GOAL,
                    context.possession(),
                    scorer.id(),
                    scorer.name(),
                    null,
                    null,
                    "Goal"
            ));
            generateAssist(context, scorer);
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

    private void generateAssist(MatchContext context, PlayerState scorer) {
        List<PlayerState> teammates = context.teamInPossession().lineup().stream()
                .filter(p -> p.role() != PlayerRole.GK)
                .filter(p -> !p.id().equals(scorer.id()))
                .toList();
        if (teammates.isEmpty()) {
            return;
        }
        PlayerState assister = teammates.get(context.random().nextInt(teammates.size()));
        context.addEvent(new SimulatedEvent(
                context.minute(),
                MatchEvents.ASSIST,
                context.possession(),
                assister.id(),
                assister.name(),
                scorer.id(),
                scorer.name(),
                "Assist"
        ));
    }

    /**
     * After a turnover the defending team may commit a foul.
     * Card probability is driven by the defender's tactics cardRisk + the tackler's physical stat.
     */
    private void rollForCard(MatchContext context) {
        TeamState defending = context.defendingTeam();
        TeamSide defendingSide = context.possession().opposite();
        double cardRisk = defending.tactics().cardRisk();

        double chance = CARD_BASE_CHANCE + cardRisk * CARD_RISK_WEIGHT;

        PlayerState tackler = context.pickCarrier(defending, context.zone());
        chance += tackler.physical() * CARD_PHYSICAL_WEIGHT;

        int roll = context.random().nextInt(100);
        if (roll >= chance) {
            return;
        }

        int yellowCount = context.addYellowCard(tackler.id());
        if (yellowCount >= 2) {
            context.addEvent(new SimulatedEvent(
                    context.minute(),
                    MatchEvents.RED_CARD,
                    defendingSide,
                    tackler.id(),
                    tackler.name(),
                    null,
                    null,
                    "Second yellow card"
            ));
        } else {
            context.addEvent(new SimulatedEvent(
                    context.minute(),
                    MatchEvents.YELLOW_CARD,
                    defendingSide,
                    tackler.id(),
                    tackler.name(),
                    null,
                    null,
                    "Yellow card"
            ));
        }
    }

    private void switchPossession(MatchContext context) {
        TeamSide next = context.possession().opposite();
        context.setPossession(next);
        context.setZone(PitchZone.MIDFIELD);
        context.setBallCarrier(context.pickCarrier(context.teamInPossession(), PitchZone.MIDFIELD));
    }
}

