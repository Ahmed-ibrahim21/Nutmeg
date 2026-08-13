package com.wr.nutmeg.league.dtos;

import java.util.List;
import java.util.UUID;

public record RoundSimulationResult(
            UUID leagueId,
            int roundSimulated,
            int matchesPlayed,
            List<FixtureResult> results
    ) {}
