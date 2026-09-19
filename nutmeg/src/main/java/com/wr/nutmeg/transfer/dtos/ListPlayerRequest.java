package com.wr.nutmeg.transfer.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ListPlayerRequest(
        @NotNull UUID playerId,
        @Min(1) long askingPrice
) {
}
