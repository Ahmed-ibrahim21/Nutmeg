package com.wr.nutmeg.league.dtos;

import java.util.List;
import java.util.UUID;

public record LeagueTableResponse(
        UUID leagueId,
        String leagueName,
        int currentRound,
        List<LeagueTableRowResponse> standings
) {
}
