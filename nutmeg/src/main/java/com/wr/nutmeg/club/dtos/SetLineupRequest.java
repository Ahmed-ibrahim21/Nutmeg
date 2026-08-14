package com.wr.nutmeg.club.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SetLineupRequest(
        @NotNull @Size(min = 11, max = 11, message = "Lineup must contain exactly 11 players")
        List<@Valid @NotNull LineupAssignmentDto> lineup
) {
}
