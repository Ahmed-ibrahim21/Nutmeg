package com.wr.nutmeg.transfer.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MakeOfferRequest(
        @NotNull UUID playerId,
        @Min(1) long offerAmount
) {
}
