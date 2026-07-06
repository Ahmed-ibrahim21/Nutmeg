package com.wr.nutmeg.match.setup;

import com.wr.nutmeg.tactics.Formation;
import com.wr.nutmeg.tactics.FormationSlot;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class FormationTemplate {

    private static final Map<Formation, List<FormationSlot>> SLOTS = new EnumMap<>(Formation.class);

    static {
        SLOTS.put(Formation.F_4_4_2, List.of(
                FormationSlot.GK,
                FormationSlot.LB, FormationSlot.CB1, FormationSlot.CB2, FormationSlot.RB,
                FormationSlot.LM, FormationSlot.CM1, FormationSlot.CM2, FormationSlot.RM,
                FormationSlot.ST1, FormationSlot.ST2
        ));
        SLOTS.put(Formation.F_4_3_3, List.of(
                FormationSlot.GK,
                FormationSlot.LB, FormationSlot.CB1, FormationSlot.CB2, FormationSlot.RB,
                FormationSlot.CM1, FormationSlot.CM2, FormationSlot.CM3,
                FormationSlot.LW, FormationSlot.ST, FormationSlot.RW
        ));
        SLOTS.put(Formation.F_4_2_3_1, List.of(
                FormationSlot.GK,
                FormationSlot.LB, FormationSlot.CB1, FormationSlot.CB2, FormationSlot.RB,
                FormationSlot.CDM, FormationSlot.CM1,
                FormationSlot.LW, FormationSlot.CAM, FormationSlot.RW,
                FormationSlot.ST
        ));
        SLOTS.put(Formation.F_5_3_2, List.of(
                FormationSlot.GK,
                FormationSlot.LWB, FormationSlot.CB1, FormationSlot.CB2, FormationSlot.CB3, FormationSlot.RWB,
                FormationSlot.CM1, FormationSlot.CM2, FormationSlot.CM3,
                FormationSlot.ST1, FormationSlot.ST2
        ));
        SLOTS.put(Formation.F_4_5_1, List.of(
                FormationSlot.GK,
                FormationSlot.LB, FormationSlot.CB1, FormationSlot.CB2, FormationSlot.RB,
                FormationSlot.LM, FormationSlot.CDM, FormationSlot.CM1, FormationSlot.CM2, FormationSlot.RM,
                FormationSlot.ST
        ));
        SLOTS.put(Formation.F_3_4_3, List.of(
                FormationSlot.GK,
                FormationSlot.CB1, FormationSlot.CB2, FormationSlot.CB3,
                FormationSlot.LM, FormationSlot.CM1, FormationSlot.CM2, FormationSlot.RM,
                FormationSlot.LW, FormationSlot.ST, FormationSlot.RW
        ));
    }

    private FormationTemplate() {
    }

    public static List<FormationSlot> slotsFor(Formation formation) {
        return SLOTS.get(formation);
    }
}
