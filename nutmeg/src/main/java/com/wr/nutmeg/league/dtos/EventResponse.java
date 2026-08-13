package com.wr.nutmeg.league.dtos;

import com.wr.nutmeg.match.engine.SimulatedEvent;

public record EventResponse(
            int minute,
            String type,
            String team,
            String player,
            String detail
    ) {
      public  static EventResponse from(SimulatedEvent event) {
            return new EventResponse(
                    event.minute(),
                    event.type().name(),
                    event.team().name(),
                    event.playerName(),
                    event.detail()
            );
        }
    }
