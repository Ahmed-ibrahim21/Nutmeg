package com.wr.nutmeg.transfer;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubLineupRepository;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.common.enums.Position;
import com.wr.nutmeg.exceptions.InvalidArgumentsException;
import com.wr.nutmeg.exceptions.InvlaidStateException;
import com.wr.nutmeg.finance.FinanceService;
import com.wr.nutmeg.finance.TransactionType;
import com.wr.nutmeg.player.Player;
import com.wr.nutmeg.player.PlayerRepository;
import com.wr.nutmeg.transfer.dtos.ListPlayerRequest;
import com.wr.nutmeg.transfer.dtos.MakeOfferRequest;
import com.wr.nutmeg.transfer.dtos.TransferCompletedResponse;
import com.wr.nutmeg.transfer.dtos.TransferListingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ClubLineupRepository clubLineupRepository;

    @Mock
    private TransferListingRepository listingRepository;

    @Mock
    private TransferOfferRepository offerRepository;

    @Mock
    private FinanceService financeService;

    @InjectMocks
    private TransferService transferService;

    private Club sellingClub;
    private Club buyingClub;
    private Player player;
    private UUID sellingClubId;
    private UUID buyingClubId;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        sellingClubId = UUID.randomUUID();
        buyingClubId = UUID.randomUUID();
        playerId = UUID.randomUUID();

        sellingClub = new Club();
        sellingClub.setId(sellingClubId);
        sellingClub.setName("Seller FC");

        buyingClub = new Club();
        buyingClub.setId(buyingClubId);
        buyingClub.setName("Buyer FC");

        player = new Player();
        player.setId(playerId);
        player.setFirstName("Alex");
        player.setLastName("Morgan");
        player.setPosition(Position.ST);
        player.setPace(80);
        player.setShooting(85);
        player.setPassing(70);
        player.setDribbling(82);
        player.setDefending(40);
        player.setPhysical(75);
        player.setStamina(78);
        player.setClub(sellingClub);
        player.setMarketValue(5_000_000L);
    }

    @Test
    void listPlayerCreatesActiveListing() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(listingRepository.findByPlayerIdAndActiveTrue(playerId)).thenReturn(Optional.empty());
        when(clubRepository.findById(sellingClubId)).thenReturn(Optional.of(sellingClub));
        when(listingRepository.save(any(TransferListing.class))).thenAnswer(invocation -> {
            TransferListing listing = invocation.getArgument(0);
            listing.setId(UUID.randomUUID());
            return listing;
        });

        TransferListingResponse response = transferService.listPlayer(
                sellingClubId,
                new ListPlayerRequest(playerId, 7_500_000L)
        );

        assertThat(response.askingPrice()).isEqualTo(7_500_000L);
        assertThat(response.playerId()).isEqualTo(playerId);

        ArgumentCaptor<TransferListing> captor = ArgumentCaptor.forClass(TransferListing.class);
        verify(listingRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isTrue();
    }

    @Test
    void makeOfferRejectsOwnPlayer() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> transferService.makeOffer(
                sellingClubId,
                new MakeOfferRequest(playerId, 1_000_000L)
        )).isInstanceOf(InvalidArgumentsException.class)
                .hasMessageContaining("own player");
    }

    @Test
    void makeOfferCreatesPendingOfferForUnlistedPlayer() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(offerRepository.findByBuyingClubIdAndPlayerIdAndStatus(
                buyingClubId, playerId, TransferOfferStatus.PENDING
        )).thenReturn(Optional.empty());
        when(clubRepository.findById(buyingClubId)).thenReturn(Optional.of(buyingClub));
        when(clubRepository.findById(sellingClubId)).thenReturn(Optional.of(sellingClub));
        when(listingRepository.findByPlayerIdAndActiveTrue(playerId)).thenReturn(Optional.empty());
        when(offerRepository.save(any(TransferOffer.class))).thenAnswer(invocation -> {
            TransferOffer offer = invocation.getArgument(0);
            offer.setId(UUID.randomUUID());
            return offer;
        });

        var response = transferService.makeOffer(
                buyingClubId,
                new MakeOfferRequest(playerId, 6_000_000L)
        );

        assertThat(response.offerAmount()).isEqualTo(6_000_000L);
        assertThat(response.fromListing()).isFalse();
        assertThat(response.status()).isEqualTo(TransferOfferStatus.PENDING);
    }

    @Test
    void acceptOfferCompletesTransferAndProcessesFinance() {
        UUID offerId = UUID.randomUUID();
        TransferOffer offer = new TransferOffer();
        offer.setId(offerId);
        offer.setPlayer(player);
        offer.setBuyingClub(buyingClub);
        offer.setSellingClub(sellingClub);
        offer.setOfferAmount(6_000_000L);
        offer.setStatus(TransferOfferStatus.PENDING);

        when(offerRepository.findByIdAndSellingClubId(offerId, sellingClubId)).thenReturn(Optional.of(offer));
        when(clubRepository.findByIdForUpdate(any())).thenReturn(Optional.of(sellingClub));
        when(playerRepository.save(player)).thenReturn(player);
        when(offerRepository.findByPlayerIdAndStatus(playerId, TransferOfferStatus.PENDING))
                .thenReturn(List.of());

        TransferCompletedResponse result = transferService.acceptOffer(sellingClubId, offerId);

        assertThat(result.fee()).isEqualTo(6_000_000L);
        assertThat(result.toClubId()).isEqualTo(buyingClubId);
        assertThat(player.getClub()).isEqualTo(buyingClub);

        verify(financeService).debit(
                eq(buyingClubId),
                eq(6_000_000L),
                eq(TransactionType.PLAYER_PURCHASE),
                any(),
                eq(offerId)
        );
        verify(financeService).credit(
                eq(sellingClubId),
                eq(6_000_000L),
                eq(TransactionType.PLAYER_SALE),
                any(),
                eq(offerId)
        );
        assertThat(offer.getStatus()).isEqualTo(TransferOfferStatus.ACCEPTED);
    }

    @Test
    void listPlayerFailsWhenAlreadyListed() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(listingRepository.findByPlayerIdAndActiveTrue(playerId))
                .thenReturn(Optional.of(new TransferListing()));

        assertThatThrownBy(() -> transferService.listPlayer(
                sellingClubId,
                new ListPlayerRequest(playerId, 5_000_000L)
        )).isInstanceOf(InvlaidStateException.class)
                .hasMessageContaining("already listed");
    }
}
