package com.wr.nutmeg.club.dtos;

import com.wr.nutmeg.tactics.Formation;

import java.util.List;

public record LineupResponse(
        Formation formation,
        List<LineupPlayerResponse> players
) {
}
