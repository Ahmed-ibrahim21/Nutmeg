package com.wr.nutmeg.league.dtos;
import com.wr.nutmeg.league.League;

import java.util.List;

import org.springframework.data.domain.Page;


public record PagedLeagueResponse(
            List<LeagueResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static PagedLeagueResponse from(Page<League> leaguePage) {
            return new PagedLeagueResponse(
                    leaguePage.getContent().stream().map(LeagueResponse::from).toList(),
                    leaguePage.getNumber(),
                    leaguePage.getSize(),
                    leaguePage.getTotalElements(),
                    leaguePage.getTotalPages()
            );
        }
    }
