package com.wr.nutmeg.transfer.dtos;

import com.wr.nutmeg.common.enums.Position;
import com.wr.nutmeg.transfer.TransferListing;

import java.time.Instant;
import java.util.UUID;

public record TransferListingResponse(
        UUID id,
        UUID playerId,
        String playerName,
        Position position,
        int overallRating,
        long marketValue,
        UUID sellingClubId,
        String sellingClubName,
        long askingPrice,
        Instant listedAt
) {
    public static TransferListingResponse from(TransferListing listing) {
        return new TransferListingResponse(
                listing.getId(),
                listing.getPlayer().getId(),
                listing.getPlayer().getFullName(),
                listing.getPlayer().getPosition(),
                listing.getPlayer().getOverallRating(),
                listing.getPlayer().getMarketValue(),
                listing.getSellingClub().getId(),
                listing.getSellingClub().getName(),
                listing.getAskingPrice(),
                listing.getListedAt()
        );
    }
}
