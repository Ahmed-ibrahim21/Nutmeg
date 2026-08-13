package com.wr.nutmeg.club.dtos;

import java.util.UUID;

import com.wr.nutmeg.club.Club;

public record ClubResponse(
            UUID id,
            String name,
            String shortName,
            String logoUrl,
            String stadiumName,
            long budget
    ) {
      public  static ClubResponse from(Club club) {
            return new ClubResponse(
                    club.getId(),
                    club.getName(),
                    club.getShortName(),
                    club.getLogoUrl(),
                    club.getStadiumName(),
                    club.getBudget()
            );
        }
    }
