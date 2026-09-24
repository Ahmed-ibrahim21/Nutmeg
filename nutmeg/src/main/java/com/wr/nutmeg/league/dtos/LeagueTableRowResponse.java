package com.wr.nutmeg.league.dtos;

import java.util.UUID;

public record LeagueTableRowResponse(
        int position,
        UUID clubId,
        String clubName,
        int played,
        int wins,
        int draws,
        int losses,
        int goalsFor,
        int goalsAgainst,
        int goalDifference,
        int points
) {
}
