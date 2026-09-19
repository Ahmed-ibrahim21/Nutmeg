package com.wr.nutmeg.finance;

import com.wr.nutmeg.auth.ManagerUserDetails;
import com.wr.nutmeg.exceptions.InvlaidStateException;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.finance.dtos.BalanceResponse;
import com.wr.nutmeg.finance.dtos.FinancialSummary;
import com.wr.nutmeg.finance.dtos.TransactionResponse;
import com.wr.nutmeg.manager.Manager;
import com.wr.nutmeg.manager.ManagerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clubs/my/finance")
@Tag(name = "Finance", description = "Financial ledger, balance, and expense tracking for the authenticated manager's club")
public class FinanceController {

    private final ManagerRepository managerRepository;
    private final FinanceService financeService;

    public FinanceController(ManagerRepository managerRepository, FinanceService financeService) {
        this.managerRepository = managerRepository;
        this.financeService = financeService;
    }

    @Operation(
            summary = "Get club balance",
            description = "Returns current financial balance for the authenticated manager's club."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance returned",
                    content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Manager not found"),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @GetMapping("/balance")
    public BalanceResponse getBalance(@AuthenticationPrincipal ManagerUserDetails principal) {
        UUID clubId = resolveClubId(principal);
        return financeService.getBalance(clubId);
    }

    @Operation(
            summary = "Get financial summary",
            description = "Returns an aggregated breakdown of club income, expenses, net profit/loss, and weekly wage bill."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Financial summary returned",
                    content = @Content(schema = @Schema(implementation = FinancialSummary.class))),
            @ApiResponse(responseCode = "404", description = "Manager not found"),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @GetMapping("/summary")
    public FinancialSummary getSummary(@AuthenticationPrincipal ManagerUserDetails principal) {
        UUID clubId = resolveClubId(principal);
        return financeService.getFinancialSummary(clubId);
    }

    @Operation(
            summary = "Get transaction history",
            description = "Returns all financial ledger transactions for the club, optionally filtered by transaction type."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Manager not found"),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactions(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @RequestParam(required = false) TransactionType type
    ) {
        UUID clubId = resolveClubId(principal);
        if (type != null) {
            return financeService.getTransactionsByType(clubId, type);
        }
        return financeService.getTransactionHistory(clubId);
    }

    @Operation(
            summary = "Pay squad wages",
            description = "Deducts weekly squad wages from club balance and records a wage payment transaction."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wage payment processed",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient funds to pay wages"),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet")
    })
    @PostMapping("/wages/pay")
    public TransactionResponse payWeeklyWages(@AuthenticationPrincipal ManagerUserDetails principal) {
        UUID clubId = resolveClubId(principal);
        FinancialTransaction tx = financeService.processWeeklyWages(clubId);
        return tx != null ? TransactionResponse.from(tx) : null;
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
