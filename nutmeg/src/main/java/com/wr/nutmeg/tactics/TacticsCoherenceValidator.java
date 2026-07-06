package com.wr.nutmeg.tactics;
import org.springframework.stereotype.Component;

@Component
public class TacticsCoherenceValidator {

    public TacticsProfile buildProfile(MatchTactics tactics) {
        double attackBias = 0;
        double defenseBias = 0;
        double possessionBias = 0;
        double pressIntensity = 0;
        double widthBias = 0;
        double tempoMultiplier = 1;
        double cardRisk = 0;
        double coherencePenalty = 0;

        FormationArchetype archetype = tactics.getFormation().getArchetype();

        attackBias += styleAttack(tactics.getStyle());
        defenseBias += styleDefense(tactics.getStyle());
        pressIntensity += pressingValue(tactics.getPressing());
        possessionBias += tempoPossession(tactics.getTempo());
        tempoMultiplier *= tempoSpeed(tactics.getTempo());
        widthBias += gamePlanWidth(tactics.getGamePlan());
        attackBias += forwardLineAttack(tactics.getForwardLine());
        defenseBias += defenseLineValue(tactics.getDefenseLine());
        cardRisk += tacklingRisk(tactics.getTackling());

        coherencePenalty += formationStyleMismatch(archetype, tactics.getStyle());
        coherencePenalty += formationGamePlanMismatch(archetype, tactics.getGamePlan());
        coherencePenalty += pressingStyleMismatch(tactics.getPressing(), tactics.getStyle());
        coherencePenalty += offsideTrapMismatch(tactics);

        if (tactics.getForwardLine() == ForwardLine.ATTACK_ONLY
                && tactics.getMidfieldLine() == MidfieldLine.PROTECT_DEFENSE) {
            coherencePenalty += 0.08;
        }

        double coherenceScore = Math.max(0.75, 1.0 - coherencePenalty);

        return new TacticsProfile(
                attackBias,
                defenseBias,
                possessionBias,
                pressIntensity,
                widthBias,
                tempoMultiplier,
                cardRisk,
                coherenceScore
        );
    }

    private double formationStyleMismatch(FormationArchetype archetype, Style style) {
        if (archetype == FormationArchetype.ATTACKING && style == Style.PARK_THE_BUS) {
            return 0.15;
        }
        if (archetype == FormationArchetype.ATTACKING && style == Style.DEFENSIVE) {
            return 0.10;
        }
        if (archetype == FormationArchetype.DEFENSIVE && style == Style.ALL_OUT_ATTACK) {
            return 0.15;
        }
        if (archetype == FormationArchetype.DEFENSIVE && style == Style.ATTACKING) {
            return 0.10;
        }
        return 0;
    }

    private double formationGamePlanMismatch(FormationArchetype archetype, GamePlan gamePlan) {
        if (archetype == FormationArchetype.DEFENSIVE && gamePlan == GamePlan.WING_PLAY) {
            return 0.08;
        }
        if (archetype == FormationArchetype.ATTACKING && gamePlan == GamePlan.LONG_BALL) {
            return 0.05;
        }
        return 0;
    }

    private double pressingStyleMismatch(Pressing pressing, Style style) {
        if (pressing == Pressing.SIT_DEEP && (style == Style.ALL_OUT_ATTACK || style == Style.ATTACKING)) {
            return 0.10;
        }
        if (pressing == Pressing.PRESS_HIGH && style == Style.PARK_THE_BUS) {
            return 0.10;
        }
        return 0;
    }

    private double offsideTrapMismatch(MatchTactics tactics) {
        if (!tactics.isOffsideTrap()) {
            return 0;
        }
        if (tactics.getFormation().getDefenders() >= 5) {
            return 0.12;
        }
        if (tactics.getPressing() == Pressing.SIT_DEEP) {
            return 0.10;
        }
        return 0;
    }

    private double styleAttack(Style style) {
        return switch (style) {
            case ALL_OUT_ATTACK -> 12;
            case ATTACKING -> 8;
            case BALANCED -> 0;
            case DEFENSIVE -> -6;
            case PARK_THE_BUS -> -10;
        };
    }

    private double styleDefense(Style style) {
        return switch (style) {
            case ALL_OUT_ATTACK -> -8;
            case ATTACKING -> -4;
            case BALANCED -> 0;
            case DEFENSIVE -> 8;
            case PARK_THE_BUS -> 12;
        };
    }

    private double pressingValue(Pressing pressing) {
        return switch (pressing) {
            case PRESS_HIGH -> 12;
            case CLOSE_DOWN -> 8;
            case BALANCED -> 0;
            case SIT_DEEP -> -8;
        };
    }

    private double tempoPossession(Tempo tempo) {
        return switch (tempo) {
            case ONE_TOUCH, HIGH_SPEED -> -4;
            case POSSESSION -> 6;
            case SLOW_BUILD_UP -> 8;
        };
    }

    private double tempoSpeed(Tempo tempo) {
        return switch (tempo) {
            case ONE_TOUCH -> 1.25;
            case HIGH_SPEED -> 1.15;
            case POSSESSION -> 1.0;
            case SLOW_BUILD_UP -> 0.85;
        };
    }

    private double gamePlanWidth(GamePlan gamePlan) {
        return switch (gamePlan) {
            case WING_PLAY -> 10;
            case PASSING_GAME -> 2;
            case COUNTER_ATTACK -> 0;
            case SHOOT_ON_SIGHT -> -2;
            case LONG_BALL -> -4;
        };
    }

    private double forwardLineAttack(ForwardLine forwardLine) {
        return switch (forwardLine) {
            case ATTACK_ONLY -> 8;
            case SUPPORT_MIDFIELD -> 0;
            case DROP_DEEP -> -6;
        };
    }

    private double defenseLineValue(DefenseLine defenseLine) {
        return switch (defenseLine) {
            case ATTACKING_FULL_BACKS -> -4;
            case SUPPORT_MIDFIELD -> 0;
            case DEFEND_DEEP -> 8;
        };
    }

    private double tacklingRisk(Tackling tackling) {
        return switch (tackling) {
            case RECKLESS -> 10;
            case AGGRESSIVE -> 6;
            case NORMAL -> 0;
            case EASY -> -4;
        };
    }
}
