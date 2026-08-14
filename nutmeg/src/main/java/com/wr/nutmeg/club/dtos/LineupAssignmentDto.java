package com.wr.nutmeg.club.dtos;

import com.wr.nutmeg.tactics.FormationSlot;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LineupAssignmentDto(
        @NotNull FormationSlot slot,
        @NotNull UUID playerId
) {
}
