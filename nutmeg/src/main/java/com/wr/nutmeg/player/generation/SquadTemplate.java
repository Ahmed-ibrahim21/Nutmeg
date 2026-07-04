package com.wr.nutmeg.player.generation;

import com.wr.nutmeg.common.enums.Position;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SquadTemplate {

    private static final Map<Position, Integer> CORE_SQUAD = Map.ofEntries(
            Map.entry(Position.GK, 2),
            Map.entry(Position.CB, 2),
            Map.entry(Position.RB, 1),
            Map.entry(Position.LB, 1),
            Map.entry(Position.CDM, 1),
            Map.entry(Position.CM, 1),
            Map.entry(Position.CAM, 1),
            Map.entry(Position.LM, 1),
            Map.entry(Position.RM, 1),
            Map.entry(Position.LW, 1),
            Map.entry(Position.RW, 1),
            Map.entry(Position.ST, 1)
    );

    private static final List<Position> FLEX_POSITIONS = List.of(
            Position.CM,
            Position.CAM,
            Position.ST,
            Position.CB,
            Position.LW,
            Position.RB
    );

    private SquadTemplate() {
    }

    public static List<Position> positionsForSquad() {
        Map<Position, Integer> counts = new LinkedHashMap<>(CORE_SQUAD);
        for (Position flexPosition : FLEX_POSITIONS) {
            counts.merge(flexPosition, 1, Integer::sum);
        }

        List<Position> positions = new ArrayList<>();
        counts.forEach((position, count) -> {
            for (int i = 0; i < count; i++) {
                positions.add(position);
            }
        });
        return positions;
    }
}
