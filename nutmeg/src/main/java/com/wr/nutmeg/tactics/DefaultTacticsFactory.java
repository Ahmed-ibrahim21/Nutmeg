package com.wr.nutmeg.tactics;

import org.springframework.stereotype.Component;

@Component
public class DefaultTacticsFactory {

    public MatchTactics forFormation(Formation formation) {
        MatchTactics tactics = new MatchTactics();
        tactics.setFormation(formation);

        return switch (formation.getArchetype()) {
            case ATTACKING -> attackingDefaults(tactics);
            case DEFENSIVE -> defensiveDefaults(tactics);
            case BALANCED -> balancedDefaults(tactics);
        };
    }

    public MatchTactics counterFormation(Formation ownFormation, Formation opponentFormation) {
        MatchTactics tactics = forFormation(ownFormation);
        if (opponentFormation.getDefenders() >= 5) {
            tactics.setGamePlan(GamePlan.WING_PLAY);
            tactics.setForwardLine(ForwardLine.ATTACK_ONLY);
            tactics.setPressing(Pressing.CLOSE_DOWN);
        } else if (opponentFormation.getForwards() >= 3) {
            tactics.setGamePlan(GamePlan.COUNTER_ATTACK);
            tactics.setDefenseLine(DefenseLine.DEFEND_DEEP);
            tactics.setPressing(Pressing.SIT_DEEP);
            tactics.setStyle(Style.DEFENSIVE);
        }
        return tactics;
    }

    private MatchTactics attackingDefaults(MatchTactics tactics) {
        tactics.setGamePlan(GamePlan.WING_PLAY);
        tactics.setForwardLine(ForwardLine.ATTACK_ONLY);
        tactics.setMidfieldLine(MidfieldLine.STAY_IN_POSITION);
        tactics.setDefenseLine(DefenseLine.ATTACKING_FULL_BACKS);
        tactics.setPressing(Pressing.CLOSE_DOWN);
        tactics.setStyle(Style.ATTACKING);
        tactics.setTempo(Tempo.HIGH_SPEED);
        tactics.setTackling(Tackling.AGGRESSIVE);
        tactics.setMarking(Marking.ZONAL);
        tactics.setOffsideTrap(false);
        return tactics;
    }

    private MatchTactics balancedDefaults(MatchTactics tactics) {
        tactics.setGamePlan(GamePlan.PASSING_GAME);
        tactics.setForwardLine(ForwardLine.SUPPORT_MIDFIELD);
        tactics.setMidfieldLine(MidfieldLine.STAY_IN_POSITION);
        tactics.setDefenseLine(DefenseLine.DEFEND_DEEP);
        tactics.setPressing(Pressing.BALANCED);
        tactics.setStyle(Style.BALANCED);
        tactics.setTempo(Tempo.POSSESSION);
        tactics.setTackling(Tackling.NORMAL);
        tactics.setMarking(Marking.ZONAL);
        tactics.setOffsideTrap(false);
        return tactics;
    }

    private MatchTactics defensiveDefaults(MatchTactics tactics) {
        tactics.setGamePlan(GamePlan.COUNTER_ATTACK);
        tactics.setForwardLine(ForwardLine.DROP_DEEP);
        tactics.setMidfieldLine(MidfieldLine.PROTECT_DEFENSE);
        tactics.setDefenseLine(DefenseLine.DEFEND_DEEP);
        tactics.setPressing(Pressing.SIT_DEEP);
        tactics.setStyle(Style.DEFENSIVE);
        tactics.setTempo(Tempo.SLOW_BUILD_UP);
        tactics.setTackling(Tackling.NORMAL);
        tactics.setMarking(Marking.ZONAL);
        tactics.setOffsideTrap(false);
        return tactics;
    }
}
