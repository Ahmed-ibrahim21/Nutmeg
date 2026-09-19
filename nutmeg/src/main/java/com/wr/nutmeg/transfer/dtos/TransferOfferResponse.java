package com.wr.nutmeg.transfer.dtos;

import com.wr.nutmeg.common.enums.Position;
import com.wr.nutmeg.transfer.TransferOffer;
import com.wr.nutmeg.transfer.TransferOfferStatus;

import java.time.Instant;
import java.util.UUID;

public record TransferOfferResponse(
        UUID id,
        UUID playerId,
        String playerName,
        Position position,
        int overallRating,
        UUID buyingClubId,
        String buyingClubName,
        UUID sellingClubId,
        String sellingClubName,
        long offerAmount,
        TransferOfferStatus status,
        boolean fromListing,
        Instant createdAt,
        Instant respondedAt
) {
    public static TransferOfferResponse from(TransferOffer offer) {
        return new TransferOfferResponse(
                offer.getId(),
                offer.getPlayer().getId(),
                offer.getPlayer().getFullName(),
                offer.getPlayer().getPosition(),
                offer.getPlayer().getOverallRating(),
                offer.getBuyingClub().getId(),
                offer.getBuyingClub().getName(),
                offer.getSellingClub().getId(),
                offer.getSellingClub().getName(),
                offer.getOfferAmount(),
                offer.getStatus(),
                offer.getListing() != null,
                offer.getCreatedAt(),
                offer.getRespondedAt()
        );
    }
}
