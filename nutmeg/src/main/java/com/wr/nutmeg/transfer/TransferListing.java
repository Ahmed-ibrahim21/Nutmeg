package com.wr.nutmeg.transfer;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "transfer_listings")
@Getter
@Setter
@NoArgsConstructor
public class TransferListing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selling_club_id", nullable = false)
    private Club sellingClub;

    @Column(nullable = false)
    private long askingPrice;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant listedAt;

    private Instant delistedAt;
}
