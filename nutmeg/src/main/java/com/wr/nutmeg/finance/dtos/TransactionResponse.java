package com.wr.nutmeg.finance.dtos;

import com.wr.nutmeg.finance.FinancialTransaction;
import com.wr.nutmeg.finance.TransactionType;

import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID clubId,
        TransactionType type,
        long amount,
        long balanceAfter,
        String description,
        Instant createdAt,
        UUID referenceId
) {
    public static TransactionResponse from(FinancialTransaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getClub().getId(),
                tx.getType(),
                tx.getAmount(),
                tx.getBalanceAfter(),
                tx.getDescription(),
                tx.getCreatedAt(),
                tx.getReferenceId()
        );
    }
}
