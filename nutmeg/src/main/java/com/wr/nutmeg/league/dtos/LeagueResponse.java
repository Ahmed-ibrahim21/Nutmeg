package com.wr.nutmeg.league.dtos;

import java.util.UUID;

import com.wr.nutmeg.league.League;

public record LeagueResponse(
            UUID id,
            String name,
            String country,
            int tier,
            int currentRound,
            int totalRounds,
            int clubsNumber,
            String status
    ) {
       public static LeagueResponse from(League league) {
            return new LeagueResponse(
                    league.getId(),
                    league.getName(),
                    league.getCountry(),
                    league.getTier(),
                    league.getCurrentRound(),
                    league.getTotalRounds(),
                    league.getClubsNumber(),
                    league.getStatus().name()
            );
        }
    }