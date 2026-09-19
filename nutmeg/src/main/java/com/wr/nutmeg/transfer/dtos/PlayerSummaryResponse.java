package com.wr.nutmeg.transfer.dtos;

import com.wr.nutmeg.common.enums.Position;
import com.wr.nutmeg.player.Player;

import java.time.LocalDate;
import java.util.UUID;

public record PlayerSummaryResponse(
        UUID id,
        String fullName,
        Position position,
        int overallRating,
        long marketValue,
        long weeklyWage,
        LocalDate contractExpiry,
        boolean listedOnMarket,
        Long askingPrice
) {
    public static PlayerSummaryResponse from(Player player, boolean listedOnMarket, Long askingPrice) {
        return new PlayerSummaryResponse(
                player.getId(),
                player.getFullName(),
                player.getPosition(),
                player.getOverallRating(),
                player.getMarketValue(),
                player.getWeeklyWage(),
                player.getContractExpiry(),
                listedOnMarket,
                askingPrice
        );
    }
}
