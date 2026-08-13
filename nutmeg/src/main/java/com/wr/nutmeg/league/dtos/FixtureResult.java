package com.wr.nutmeg.league.dtos;

import java.util.UUID;

public record FixtureResult(
            UUID fixtureId,
            String homeClub,
            String awayClub,
            int homeScore,
            int awayScore
    ) {}
