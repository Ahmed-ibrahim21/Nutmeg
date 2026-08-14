package com.wr.nutmeg.club.dtos;

import com.wr.nutmeg.common.enums.Position;
import com.wr.nutmeg.tactics.FormationSlot;

import java.util.UUID;

public record LineupPlayerResponse(
        FormationSlot slot,
        UUID playerId,
        String playerName,
        Position position,
        int overallRating
) {
}
