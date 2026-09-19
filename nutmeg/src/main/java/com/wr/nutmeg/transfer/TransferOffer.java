package com.wr.nutmeg.transfer;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfer_offers")
@Getter
@Setter
@NoArgsConstructor
public class TransferOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buying_club_id", nullable = false)
    private Club buyingClub;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selling_club_id", nullable = false)
    private Club sellingClub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private TransferListing listing;

    @Column(nullable = false)
    private long offerAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferOfferStatus status = TransferOfferStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant respondedAt;
}
