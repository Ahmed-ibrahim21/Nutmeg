package com.wr.nutmeg.league.dtos;

import java.util.List;
import com.wr.nutmeg.match.engine.MatchResult;

public record SimulationResponse(
            int homeScore,
            int awayScore,
            int homePossessions,
            int awayPossessions,
            List<EventResponse> events
    ) {
        public static SimulationResponse from(MatchResult result) {
            return new SimulationResponse(
                    result.homeScore(),
                    result.awayScore(),
                    result.homePossessions(),
                    result.awayPossessions(),
                    result.events().stream().map(EventResponse::from).toList()
            );
        }
    }
