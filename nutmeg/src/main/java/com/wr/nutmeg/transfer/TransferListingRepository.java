package com.wr.nutmeg.transfer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferListingRepository extends JpaRepository<TransferListing, UUID> {

    List<TransferListing> findByActiveTrueOrderByListedAtDesc();

    List<TransferListing> findBySellingClubIdAndActiveTrueOrderByListedAtDesc(UUID sellingClubId);

    Optional<TransferListing> findByPlayerIdAndActiveTrue(UUID playerId);

    Optional<TransferListing> findByIdAndSellingClubIdAndActiveTrue(UUID id, UUID sellingClubId);
}
