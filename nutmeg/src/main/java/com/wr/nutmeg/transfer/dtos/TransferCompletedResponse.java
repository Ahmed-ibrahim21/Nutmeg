package com.wr.nutmeg.transfer.dtos;

import java.util.UUID;

public record TransferCompletedResponse(
        UUID offerId,
        UUID playerId,
        String playerName,
        UUID fromClubId,
        String fromClubName,
        UUID toClubId,
        String toClubName,
        long fee
) {
}
