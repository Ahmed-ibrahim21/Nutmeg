package com.wr.nutmeg.transfer;

import com.wr.nutmeg.auth.ManagerUserDetails;
import com.wr.nutmeg.exceptions.InvlaidStateException;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.manager.Manager;
import com.wr.nutmeg.manager.ManagerRepository;
import com.wr.nutmeg.transfer.dtos.ListPlayerRequest;
import com.wr.nutmeg.transfer.dtos.MakeOfferRequest;
import com.wr.nutmeg.transfer.dtos.PlayerSummaryResponse;
import com.wr.nutmeg.transfer.dtos.TransferCompletedResponse;
import com.wr.nutmeg.transfer.dtos.TransferListingResponse;
import com.wr.nutmeg.transfer.dtos.TransferOfferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Transfers", description = "Transfer market listings, offers, and player sales")
public class TransferController {

    private final ManagerRepository managerRepository;
    private final TransferService transferService;

    public TransferController(ManagerRepository managerRepository, TransferService transferService) {
        this.managerRepository = managerRepository;
        this.transferService = transferService;
    }

    @Operation(
            summary = "Browse transfer market",
            description = "Returns all players currently listed for sale on the transfer market."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Market listings returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransferListingResponse.class))))
    })
    @GetMapping("/transfers/market")
    public List<TransferListingResponse> browseMarket() {
        return transferService.browseMarket();
    }

    @Operation(
            summary = "Browse a club's squad",
            description = "Returns players at a given club, including whether each is listed on the transfer market. Useful for scouting targets before making an offer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Club squad returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlayerSummaryResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Club not found"),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @GetMapping("/transfers/clubs/{clubId}/squad")
    public List<PlayerSummaryResponse> getClubSquad(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @PathVariable UUID clubId
    ) {
        UUID myClubId = resolveClubId(principal);
        return transferService.getClubSquad(clubId, myClubId);
    }

    @Operation(
            summary = "Get squad for transfers",
            description = "Returns the manager's squad with transfer market status for each player."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Squad returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlayerSummaryResponse.class)))),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @GetMapping("/clubs/my/transfers/squad")
    public List<PlayerSummaryResponse> getSquad(@AuthenticationPrincipal ManagerUserDetails principal) {
        UUID clubId = resolveClubId(principal);
        return transferService.getSquad(clubId);
    }

    @Operation(
            summary = "Get my transfer listings",
            description = "Returns players the manager's club has listed on the transfer market."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listings returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransferListingResponse.class)))),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @GetMapping("/clubs/my/transfers/listings")
    public List<TransferListingResponse> getMyListings(@AuthenticationPrincipal ManagerUserDetails principal) {
        UUID clubId = resolveClubId(principal);
        return transferService.getMyListings(clubId);
    }

    @Operation(
            summary = "List a player for sale",
            description = "Adds one of the manager's players to the transfer market with an asking price."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Player listed",
                    content = @Content(schema = @Schema(implementation = TransferListingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or player not in club"),
            @ApiResponse(responseCode = "409", description = "Player already listed or manager has no club")
    })
    @PostMapping("/clubs/my/transfers/listings")
    public TransferListingResponse listPlayer(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @Valid @RequestBody ListPlayerRequest request
    ) {
        UUID clubId = resolveClubId(principal);
        return transferService.listPlayer(clubId, request);
    }

    @Operation(
            summary = "Remove a player from the transfer market",
            description = "Delists a player and cancels any pending offers for that player."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Player delisted"),
            @ApiResponse(responseCode = "404", description = "Listing not found"),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @DeleteMapping("/clubs/my/transfers/listings/{listingId}")
    public void delistPlayer(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @PathVariable UUID listingId
    ) {
        UUID clubId = resolveClubId(principal);
        transferService.delistPlayer(clubId, listingId);
    }

    @Operation(
            summary = "Make a transfer offer",
            description = "Submits a bid for any player at another club, whether or not they are listed on the market."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer submitted",
                    content = @Content(schema = @Schema(implementation = TransferOfferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid target player"),
            @ApiResponse(responseCode = "409", description = "Duplicate pending offer or manager has no club")
    })
    @PostMapping("/clubs/my/transfers/offers")
    public TransferOfferResponse makeOffer(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @Valid @RequestBody MakeOfferRequest request
    ) {
        UUID clubId = resolveClubId(principal);
        return transferService.makeOffer(clubId, request);
    }

    @Operation(
            summary = "Get incoming offers",
            description = "Returns pending transfer offers received for the manager's players."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incoming offers returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransferOfferResponse.class)))),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @GetMapping("/clubs/my/transfers/offers/incoming")
    public List<TransferOfferResponse> getIncomingOffers(@AuthenticationPrincipal ManagerUserDetails principal) {
        UUID clubId = resolveClubId(principal);
        return transferService.getIncomingOffers(clubId);
    }

    @Operation(
            summary = "Get outgoing offers",
            description = "Returns pending transfer offers made by the manager's club."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Outgoing offers returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransferOfferResponse.class)))),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @GetMapping("/clubs/my/transfers/offers/outgoing")
    public List<TransferOfferResponse> getOutgoingOffers(@AuthenticationPrincipal ManagerUserDetails principal) {
        UUID clubId = resolveClubId(principal);
        return transferService.getOutgoingOffers(clubId);
    }

    @Operation(
            summary = "Accept a transfer offer",
            description = "Accepts an incoming offer, transfers the player, and processes the fee between clubs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed",
                    content = @Content(schema = @Schema(implementation = TransferCompletedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found"),
            @ApiResponse(responseCode = "406", description = "Buyer has insufficient funds"),
            @ApiResponse(responseCode = "409", description = "Offer no longer pending or player unavailable")
    })
    @PostMapping("/clubs/my/transfers/offers/{offerId}/accept")
    public TransferCompletedResponse acceptOffer(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @PathVariable UUID offerId
    ) {
        UUID clubId = resolveClubId(principal);
        return transferService.acceptOffer(clubId, offerId);
    }

    @Operation(
            summary = "Reject a transfer offer",
            description = "Declines an incoming transfer offer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer rejected",
                    content = @Content(schema = @Schema(implementation = TransferOfferResponse.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found"),
            @ApiResponse(responseCode = "409", description = "Offer no longer pending")
    })
    @PostMapping("/clubs/my/transfers/offers/{offerId}/reject")
    public TransferOfferResponse rejectOffer(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @PathVariable UUID offerId
    ) {
        UUID clubId = resolveClubId(principal);
        return transferService.rejectOffer(clubId, offerId);
    }

    @Operation(
            summary = "Withdraw a transfer offer",
            description = "Cancels an outgoing transfer offer before it is accepted or rejected."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer withdrawn",
                    content = @Content(schema = @Schema(implementation = TransferOfferResponse.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found"),
            @ApiResponse(responseCode = "409", description = "Offer no longer pending")
    })
    @DeleteMapping("/clubs/my/transfers/offers/{offerId}")
    public TransferOfferResponse withdrawOffer(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @PathVariable UUID offerId
    ) {
        UUID clubId = resolveClubId(principal);
        return transferService.withdrawOffer(clubId, offerId);
    }

    private UUID resolveClubId(ManagerUserDetails principal) {
        Manager manager = managerRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + principal.getId()));
        if (manager.getClub() == null) {
            throw new InvlaidStateException("Manager does not manage any club yet");
        }
        return manager.getClub().getId();
    }
}
