package com.wr.nutmeg.transfer;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubLineupRepository;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.exceptions.InvalidArgumentsException;
import com.wr.nutmeg.exceptions.InvlaidStateException;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.finance.FinanceService;
import com.wr.nutmeg.finance.TransactionType;
import com.wr.nutmeg.finance.WageEnforcementService;
import com.wr.nutmeg.match.setup.LineupAssignment;
import com.wr.nutmeg.player.Player;
import com.wr.nutmeg.player.PlayerRepository;
import com.wr.nutmeg.player.PlayerValuationService;
import com.wr.nutmeg.transfer.dtos.ListPlayerRequest;
import com.wr.nutmeg.transfer.dtos.MakeOfferRequest;
import com.wr.nutmeg.transfer.dtos.PlayerSummaryResponse;
import com.wr.nutmeg.transfer.dtos.TransferCompletedResponse;
import com.wr.nutmeg.transfer.dtos.TransferListingResponse;
import com.wr.nutmeg.transfer.dtos.TransferOfferResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransferService {

    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;
    private final ClubLineupRepository clubLineupRepository;
    private final TransferListingRepository listingRepository;
    private final TransferOfferRepository offerRepository;
    private final FinanceService financeService;
    private final WageEnforcementService wageEnforcementService;
    private final PlayerValuationService valuationService;

    public TransferService(
            ClubRepository clubRepository,
            PlayerRepository playerRepository,
            ClubLineupRepository clubLineupRepository,
            TransferListingRepository listingRepository,
            TransferOfferRepository offerRepository,
            FinanceService financeService,
            WageEnforcementService wageEnforcementService,
            PlayerValuationService valuationService
    ) {
        this.clubRepository = clubRepository;
        this.playerRepository = playerRepository;
        this.clubLineupRepository = clubLineupRepository;
        this.listingRepository = listingRepository;
        this.offerRepository = offerRepository;
        this.financeService = financeService;
        this.wageEnforcementService = wageEnforcementService;
        this.valuationService = valuationService;
    }

    @Transactional(readOnly = true)
    public List<TransferListingResponse> browseMarket() {
        return listingRepository.findByActiveTrueOrderByListedAtDesc()
                .stream()
                .map(TransferListingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlayerSummaryResponse> getClubSquad(UUID clubId, UUID requestingClubId) {
        if (clubId.equals(requestingClubId)) {
            return getSquad(clubId);
        }

        loadClub(clubId);
        List<Player> squad = playerRepository.findByClubId(clubId);
        return squad.stream()
                .map(player -> {
                    TransferListing listing = listingRepository.findByPlayerIdAndActiveTrue(player.getId()).orElse(null);
                    return PlayerSummaryResponse.from(
                            player,
                            listing != null,
                            listing != null ? listing.getAskingPrice() : null
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlayerSummaryResponse> getSquad(UUID clubId) {
        List<Player> squad = playerRepository.findByClubId(clubId);
        Map<UUID, TransferListing> activeListings = listingRepository
                .findBySellingClubIdAndActiveTrueOrderByListedAtDesc(clubId)
                .stream()
                .collect(Collectors.toMap(listing -> listing.getPlayer().getId(), Function.identity()));

        return squad.stream()
                .map(player -> {
                    TransferListing listing = activeListings.get(player.getId());
                    return PlayerSummaryResponse.from(
                            player,
                            listing != null,
                            listing != null ? listing.getAskingPrice() : null
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransferListingResponse> getMyListings(UUID clubId) {
        return listingRepository.findBySellingClubIdAndActiveTrueOrderByListedAtDesc(clubId)
                .stream()
                .map(TransferListingResponse::from)
                .toList();
    }

    @Transactional
    public TransferListingResponse listPlayer(UUID clubId, ListPlayerRequest request) {
        Player player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + request.playerId()));

        validatePlayerOwnership(player, clubId);

        if (listingRepository.findByPlayerIdAndActiveTrue(player.getId()).isPresent()) {
            throw new InvlaidStateException("Player is already listed on the transfer market");
        }

        Club club = loadClub(clubId);

        TransferListing listing = new TransferListing();
        listing.setPlayer(player);
        listing.setSellingClub(club);
        listing.setAskingPrice(request.askingPrice());
        listing.setActive(true);
        listing.setListedAt(Instant.now());

        TransferListing saved = listingRepository.save(listing);
        log.info("Club {} listed player {} for {}", club.getName(), player.getFullName(), request.askingPrice());
        return TransferListingResponse.from(saved);
    }

    @Transactional
    public void delistPlayer(UUID clubId, UUID listingId) {
        TransferListing listing = listingRepository.findByIdAndSellingClubIdAndActiveTrue(listingId, clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Active listing not found: " + listingId));

        listing.setActive(false);
        listing.setDelistedAt(Instant.now());
        listingRepository.save(listing);

        cancelPendingOffersForPlayer(listing.getPlayer().getId(), TransferOfferStatus.CANCELLED);
        log.info("Club {} delisted player {}", listing.getSellingClub().getName(), listing.getPlayer().getFullName());
    }

    @Transactional
    public TransferOfferResponse makeOffer(UUID buyingClubId, MakeOfferRequest request) {
        Player player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + request.playerId()));

        if (player.getClub() == null) {
            throw new InvalidArgumentsException("Player is not contracted to any club");
        }

        UUID sellingClubId = player.getClub().getId();
        if (sellingClubId.equals(buyingClubId)) {
            throw new InvalidArgumentsException("You cannot make an offer for your own player");
        }

        if (offerRepository.findByBuyingClubIdAndPlayerIdAndStatus(
                buyingClubId, player.getId(), TransferOfferStatus.PENDING
        ).isPresent()) {
            throw new InvlaidStateException("You already have a pending offer for this player");
        }

        Club buyingClub = loadClub(buyingClubId);
        Club sellingClub = loadClub(sellingClubId);
        TransferListing listing = listingRepository.findByPlayerIdAndActiveTrue(player.getId()).orElse(null);

        TransferOffer offer = new TransferOffer();
        offer.setPlayer(player);
        offer.setBuyingClub(buyingClub);
        offer.setSellingClub(sellingClub);
        offer.setListing(listing);
        offer.setOfferAmount(request.offerAmount());
        offer.setStatus(TransferOfferStatus.PENDING);
        offer.setCreatedAt(Instant.now());

        TransferOffer saved = offerRepository.save(offer);
        log.info("Club {} offered {} for player {} (owned by {})",
                buyingClub.getName(), request.offerAmount(), player.getFullName(), sellingClub.getName());
        return TransferOfferResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TransferOfferResponse> getIncomingOffers(UUID clubId) {
        return offerRepository.findBySellingClubIdAndStatusOrderByCreatedAtDesc(clubId, TransferOfferStatus.PENDING)
                .stream()
                .map(TransferOfferResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransferOfferResponse> getOutgoingOffers(UUID clubId) {
        return offerRepository.findByBuyingClubIdAndStatusOrderByCreatedAtDesc(clubId, TransferOfferStatus.PENDING)
                .stream()
                .map(TransferOfferResponse::from)
                .toList();
    }

    @Transactional
    public TransferCompletedResponse acceptOffer(UUID sellingClubId, UUID offerId) {
        TransferOffer offer = offerRepository.findByIdAndSellingClubId(offerId, sellingClubId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));

        if (offer.getStatus() != TransferOfferStatus.PENDING) {
            throw new InvlaidStateException("Offer is no longer pending");
        }

        Player player = offer.getPlayer();
        if (player.getClub() == null || !player.getClub().getId().equals(sellingClubId)) {
            throw new InvlaidStateException("Player is no longer at the selling club");
        }

        UUID buyingClubId = offer.getBuyingClub().getId();
        lockClubsInOrder(buyingClubId, sellingClubId);

        long fee = offer.getOfferAmount();
        String playerName = player.getFullName();
        String buyerName = offer.getBuyingClub().getName();
        String sellerName = offer.getSellingClub().getName();

        financeService.debit(
                buyingClubId,
                fee,
                TransactionType.PLAYER_PURCHASE,
                "Transfer fee for " + playerName + " from " + sellerName,
                offer.getId()
        );
        financeService.credit(
                sellingClubId,
                fee,
                TransactionType.PLAYER_SALE,
                "Transfer fee for " + playerName + " to " + buyerName,
                offer.getId()
        );

        Club buyingClub = offer.getBuyingClub();
        player.setClub(buyingClub);
        valuationService.revalue(player);  // recalculate value for the new club context
        playerRepository.save(player);

        removePlayerFromLineup(sellingClubId, player.getId());
        deactivateListingForPlayer(player.getId());
        resolveOffer(offer, TransferOfferStatus.ACCEPTED);
        cancelOtherPendingOffers(player.getId(), offer.getId());

        log.info("Transfer completed: {} moved from {} to {} for {}",
                playerName, sellerName, buyerName, fee);

        return new TransferCompletedResponse(
                offer.getId(),
                player.getId(),
                playerName,
                sellingClubId,
                sellerName,
                buyingClubId,
                buyerName,
                fee
        );
    }

    @Transactional
    public TransferOfferResponse rejectOffer(UUID sellingClubId, UUID offerId) {
        TransferOffer offer = offerRepository.findByIdAndSellingClubId(offerId, sellingClubId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));

        if (offer.getStatus() != TransferOfferStatus.PENDING) {
            throw new InvlaidStateException("Offer is no longer pending");
        }

        resolveOffer(offer, TransferOfferStatus.REJECTED);
        return TransferOfferResponse.from(offer);
    }

    @Transactional
    public TransferOfferResponse withdrawOffer(UUID buyingClubId, UUID offerId) {
        TransferOffer offer = offerRepository.findByIdAndBuyingClubId(offerId, buyingClubId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));

        if (offer.getStatus() != TransferOfferStatus.PENDING) {
            throw new InvlaidStateException("Offer is no longer pending");
        }

        resolveOffer(offer, TransferOfferStatus.WITHDRAWN);
        return TransferOfferResponse.from(offer);
    }

    private void validatePlayerOwnership(Player player, UUID clubId) {
        if (player.getClub() == null || !player.getClub().getId().equals(clubId)) {
            throw new InvalidArgumentsException("Player does not belong to your club");
        }
    }

    private Club loadClub(UUID clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
    }

    private void lockClubsInOrder(UUID firstClubId, UUID secondClubId) {
        UUID lower = firstClubId.compareTo(secondClubId) < 0 ? firstClubId : secondClubId;
        UUID higher = firstClubId.compareTo(secondClubId) < 0 ? secondClubId : firstClubId;
        clubRepository.findByIdForUpdate(lower)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + lower));
        clubRepository.findByIdForUpdate(higher)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + higher));
    }

    private void removePlayerFromLineup(UUID clubId, UUID playerId) {
        clubLineupRepository.findByClubId(clubId).ifPresent(lineup -> {
            List<LineupAssignment> updated = lineup.getLineup().stream()
                    .filter(assignment -> !assignment.getPlayerId().equals(playerId))
                    .toList();
            if (updated.size() != lineup.getLineup().size()) {
                lineup.getLineup().clear();
                lineup.getLineup().addAll(updated);
                clubLineupRepository.save(lineup);
            }
        });
    }

    private void deactivateListingForPlayer(UUID playerId) {
        listingRepository.findByPlayerIdAndActiveTrue(playerId).ifPresent(listing -> {
            listing.setActive(false);
            listing.setDelistedAt(Instant.now());
            listingRepository.save(listing);
        });
    }

    private void resolveOffer(TransferOffer offer, TransferOfferStatus status) {
        offer.setStatus(status);
        offer.setRespondedAt(Instant.now());
        offerRepository.save(offer);
    }

    private void cancelPendingOffersForPlayer(UUID playerId, TransferOfferStatus status) {
        List<TransferOffer> pendingOffers = offerRepository.findByPlayerIdAndStatus(
                playerId, TransferOfferStatus.PENDING
        );
        for (TransferOffer pending : pendingOffers) {
            resolveOffer(pending, status);
        }
    }

    private void cancelOtherPendingOffers(UUID playerId, UUID acceptedOfferId) {
        List<TransferOffer> pendingOffers = offerRepository.findByPlayerIdAndStatus(
                playerId, TransferOfferStatus.PENDING
        );
        for (TransferOffer pending : pendingOffers) {
            if (!pending.getId().equals(acceptedOfferId)) {
                resolveOffer(pending, TransferOfferStatus.CANCELLED);
            }
        }
    }
}
