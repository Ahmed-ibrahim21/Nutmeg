package com.wr.nutmeg.tactics.dtos;

import com.wr.nutmeg.tactics.DefenseLine;
import com.wr.nutmeg.tactics.Formation;
import com.wr.nutmeg.tactics.ForwardLine;
import com.wr.nutmeg.tactics.GamePlan;
import com.wr.nutmeg.tactics.Marking;
import com.wr.nutmeg.tactics.MatchTactics;
import com.wr.nutmeg.tactics.MidfieldLine;
import com.wr.nutmeg.tactics.Pressing;
import com.wr.nutmeg.tactics.Style;
import com.wr.nutmeg.tactics.Tackling;
import com.wr.nutmeg.tactics.Tempo;

public record TacticsResponse(
        Formation formation,
        GamePlan gamePlan,
        ForwardLine forwardLine,
        MidfieldLine midfieldLine,
        DefenseLine defenseLine,
        Pressing pressing,
        Style style,
        Tempo tempo,
        Tackling tackling,
        Marking marking,
        boolean offsideTrap,
        double coherenceScore
) {
    public static TacticsResponse from(MatchTactics tactics, double coherenceScore) {
        return new TacticsResponse(
                tactics.getFormation(),
                tactics.getGamePlan(),
                tactics.getForwardLine(),
                tactics.getMidfieldLine(),
                tactics.getDefenseLine(),
                tactics.getPressing(),
                tactics.getStyle(),
                tactics.getTempo(),
                tactics.getTackling(),
                tactics.getMarking(),
                tactics.isOffsideTrap(),
                coherenceScore
        );
    }
}
