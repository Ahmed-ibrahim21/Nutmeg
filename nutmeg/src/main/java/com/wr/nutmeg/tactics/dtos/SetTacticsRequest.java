package com.wr.nutmeg.tactics.dtos;

import com.wr.nutmeg.tactics.DefenseLine;
import com.wr.nutmeg.tactics.Formation;
import com.wr.nutmeg.tactics.ForwardLine;
import com.wr.nutmeg.tactics.GamePlan;
import com.wr.nutmeg.tactics.Marking;
import com.wr.nutmeg.tactics.MidfieldLine;
import com.wr.nutmeg.tactics.Pressing;
import com.wr.nutmeg.tactics.Style;
import com.wr.nutmeg.tactics.Tackling;
import com.wr.nutmeg.tactics.Tempo;
import jakarta.validation.constraints.NotNull;

public record SetTacticsRequest(
        @NotNull Formation formation,
        @NotNull GamePlan gamePlan,
        @NotNull ForwardLine forwardLine,
        @NotNull MidfieldLine midfieldLine,
        @NotNull DefenseLine defenseLine,
        @NotNull Pressing pressing,
        @NotNull Style style,
        @NotNull Tempo tempo,
        @NotNull Tackling tackling,
        @NotNull Marking marking,
        @NotNull Boolean offsideTrap
) {
}
