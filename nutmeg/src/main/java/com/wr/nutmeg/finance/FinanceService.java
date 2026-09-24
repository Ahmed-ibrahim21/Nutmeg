package com.wr.nutmeg.finance;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.exceptions.InvalidArgumentsException;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.finance.dtos.BalanceResponse;
import com.wr.nutmeg.finance.dtos.FinancialSummary;
import com.wr.nutmeg.finance.dtos.TransactionResponse;
import com.wr.nutmeg.match.Fixture;
import com.wr.nutmeg.player.Player;
import com.wr.nutmeg.player.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FinanceService {

    private final ClubRepository clubRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final PlayerRepository playerRepository;

    public FinanceService(
            ClubRepository clubRepository,
            FinancialTransactionRepository transactionRepository,
            PlayerRepository playerRepository
    ) {
        this.clubRepository = clubRepository;
        this.transactionRepository = transactionRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public FinancialTransaction credit(
            UUID clubId,
            long amount,
            TransactionType type,
            String description,
            UUID referenceId
    ) {
        if (amount <= 0) {
            throw new InvalidArgumentsException("Credit amount must be positive, got: " + amount);
        }

        Club club = clubRepository.findByIdForUpdate(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));

        long newBalance = club.getBalance() + amount;
        club.setBalance(newBalance);
        clubRepository.save(club);

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setClub(club);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(description);
        transaction.setCreatedAt(Instant.now());
        transaction.setReferenceId(referenceId);

        FinancialTransaction saved = transactionRepository.save(transaction);
        log.info("Credited {} to club {} ({}). New balance: {}", amount, club.getName(), type, newBalance);
        return saved;
    }

    @Transactional
    public FinancialTransaction debit(
            UUID clubId,
            long amount,
            TransactionType type,
            String description,
            UUID referenceId
    ) {
        if (amount <= 0) {
            throw new InvalidArgumentsException("Debit amount must be positive, got: " + amount);
        }

        Club club = clubRepository.findByIdForUpdate(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));

        if (club.getBalance() < amount) {
            throw new InsufficientFundsException(
                    "Club '" + club.getName() + "' has insufficient funds. Available: "
                            + club.getBalance() + ", requested: " + amount
            );
        }

        long newBalance = club.getBalance() - amount;
        club.setBalance(newBalance);
        clubRepository.save(club);

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setClub(club);
        transaction.setType(type);
        transaction.setAmount(-amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(description);
        transaction.setCreatedAt(Instant.now());
        transaction.setReferenceId(referenceId);

        FinancialTransaction saved = transactionRepository.save(transaction);
        log.info("Debited {} from club {} ({}). New balance: {}", amount, club.getName(), type, newBalance);
        return saved;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        return new BalanceResponse(club.getId(), club.getName(), club.getBalance());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(UUID clubId) {
        if (!clubRepository.existsById(clubId)) {
            throw new ResourceNotFoundException("Club not found: " + clubId);
        }
        return transactionRepository.findByClubIdOrderByCreatedAtDesc(clubId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByType(UUID clubId, TransactionType type) {
        if (!clubRepository.existsById(clubId)) {
            throw new ResourceNotFoundException("Club not found: " + clubId);
        }
        return transactionRepository.findByClubIdAndTypeOrderByCreatedAtDesc(clubId, type)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long calculateWeeklyWageBill(UUID clubId) {
        List<Player> squad = playerRepository.findByClubId(clubId);
        return squad.stream()
                .mapToLong(Player::getWeeklyWage)
                .sum();
    }

    @Transactional
    public FinancialTransaction processWeeklyWages(UUID clubId) {
        List<Player> squad = playerRepository.findByClubId(clubId);
        long wageBill = squad.stream()
                .mapToLong(Player::getWeeklyWage)
                .sum();

        if (wageBill <= 0) {
            log.info("No squad wages to pay for club ID {}", clubId);
            return null;
        }

        return debit(
                clubId,
                wageBill,
                TransactionType.WAGE_PAYMENT,
                "Weekly squad wage payment for " + squad.size() + " players",
                null
        );
    }

    @Transactional
    public FinancialTransaction processMatchDayRevenue(Fixture fixture) {
        Club homeClub = fixture.getHomeClub();
        if (homeClub == null) {
            return null;
        }

        int capacity = homeClub.getStadiumCapacity();
        int tier = fixture.getLeague() != null ? fixture.getLeague().getTier() : 1;

        int hash = fixture.getId() != null ? Math.abs(fixture.getId().hashCode()) : 0;
        double attendancePercent = 0.75 + (hash % 25) / 100.0;
        long attendance = Math.max(1, Math.round(capacity * attendancePercent));

        long ticketPrice = 15L + (tier * 5L);
        long matchDayRevenue = attendance * ticketPrice;

        String awayTeamName = fixture.getAwayClub() != null ? fixture.getAwayClub().getName() : "Opponent";
        String description = "Match-day gate receipts: " + attendance + " attendees vs " + awayTeamName;

        return credit(
                homeClub.getId(),
                matchDayRevenue,
                TransactionType.MATCH_DAY_REVENUE,
                description,
                fixture.getId()
        );
    }

    @Transactional
    public void processMatchBonuses(Fixture fixture) {
        int tier = fixture.getLeague() != null ? fixture.getLeague().getTier() : 1;
        long winBonus = switch (tier) {
            case 1 -> 50_000L;
            case 2 -> 25_000L;
            case 3 -> 10_000L;
            default -> 5_000L;
        };
        long drawBonus = winBonus / 3;

        if (fixture.getHomeScore() > fixture.getAwayScore()) {
            credit(fixture.getHomeClub().getId(), winBonus, TransactionType.WIN_BONUS, "Match win bonus vs " + fixture.getAwayClub().getName(), fixture.getId());
        } else if (fixture.getAwayScore() > fixture.getHomeScore()) {
            credit(fixture.getAwayClub().getId(), winBonus, TransactionType.WIN_BONUS, "Match win bonus vs " + fixture.getHomeClub().getName(), fixture.getId());
        } else {
            credit(fixture.getHomeClub().getId(), drawBonus, TransactionType.DRAW_BONUS, "Match draw bonus vs " + fixture.getAwayClub().getName(), fixture.getId());
            credit(fixture.getAwayClub().getId(), drawBonus, TransactionType.DRAW_BONUS, "Match draw bonus vs " + fixture.getHomeClub().getName(), fixture.getId());
        }
    }

    @Transactional
    public void processCommercialRevenue(UUID leagueId) {
        List<Club> clubs = clubRepository.findByLeagueId(leagueId);
        if (clubs.isEmpty()) return;
        
        int tier = clubs.get(0).getLeague().getTier();
        long commercialRevenue = 50_000L * tier;
        
        for (Club club : clubs) {
            credit(club.getId(), commercialRevenue, TransactionType.COMMERCIAL_REVENUE, "Weekly commercial revenue", null);
        }
    }

    @Transactional
    public void processSeasonEndPrizeMoney(UUID leagueId) {
        List<Club> clubs = clubRepository.findByLeagueId(leagueId);
        if (clubs.isEmpty()) return;
        
        int tier = clubs.get(0).getLeague().getTier();
        long prizeMoney = 5_000_000L * tier;
        
        for (Club club : clubs) {
            credit(club.getId(), prizeMoney, TransactionType.PRIZE_MONEY, "End of season prize money distribution", null);
        }
    }

    @Transactional(readOnly = true)
    public FinancialSummary getFinancialSummary(UUID clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));

        List<FinancialTransaction> transactions = transactionRepository.findByClubIdOrderByCreatedAtDesc(clubId);

        long totalIncome = 0;
        long totalExpenses = 0;
        Map<TransactionType, Long> breakdown = new EnumMap<>(TransactionType.class);

        for (FinancialTransaction tx : transactions) {
            long amount = tx.getAmount();
            if (amount > 0) {
                totalIncome += amount;
            } else {
                totalExpenses += Math.abs(amount);
            }
            breakdown.merge(tx.getType(), amount, Long::sum);
        }

        long weeklyWageBill = calculateWeeklyWageBill(clubId);
        long netProfitOrLoss = totalIncome - totalExpenses;
        return new FinancialSummary(
                club.getBalance(),
                totalIncome,
                totalExpenses,
                netProfitOrLoss,
                weeklyWageBill,
                breakdown
        );
    }
}
