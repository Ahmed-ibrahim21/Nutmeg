package com.wr.nutmeg.match.engine;

import com.wr.nutmeg.common.enums.MatchEvents;

import java.util.UUID;

public record SimulatedEvent(
        int minute,
        MatchEvents type,
        TeamSide team,
        UUID playerId,
        String playerName,
        UUID relatedPlayerId,
        String relatedPlayerName,
        String detail
) {
}
