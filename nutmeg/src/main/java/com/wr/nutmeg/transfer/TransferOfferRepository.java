package com.wr.nutmeg.transfer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferOfferRepository extends JpaRepository<TransferOffer, UUID> {

    List<TransferOffer> findBySellingClubIdAndStatusOrderByCreatedAtDesc(
            UUID sellingClubId,
            TransferOfferStatus status
    );

    List<TransferOffer> findByBuyingClubIdAndStatusOrderByCreatedAtDesc(
            UUID buyingClubId,
            TransferOfferStatus status
    );

    List<TransferOffer> findByPlayerIdAndStatus(UUID playerId, TransferOfferStatus status);

    Optional<TransferOffer> findByBuyingClubIdAndPlayerIdAndStatus(
            UUID buyingClubId,
            UUID playerId,
            TransferOfferStatus status
    );

    Optional<TransferOffer> findByIdAndSellingClubId(UUID id, UUID sellingClubId);

    Optional<TransferOffer> findByIdAndBuyingClubId(UUID id, UUID buyingClubId);
}
