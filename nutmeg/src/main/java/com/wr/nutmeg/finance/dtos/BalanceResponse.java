package com.wr.nutmeg.finance.dtos;

import java.util.UUID;

public record BalanceResponse(
        UUID clubId,
        String clubName,
        long balance
) {}
