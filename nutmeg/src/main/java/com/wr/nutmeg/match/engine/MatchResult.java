package com.wr.nutmeg.match.engine;

import java.util.List;

public record MatchResult(
        int homeScore,
        int awayScore,
        int homePossessions,
        int awayPossessions,
        List<SimulatedEvent> events
) {
}
