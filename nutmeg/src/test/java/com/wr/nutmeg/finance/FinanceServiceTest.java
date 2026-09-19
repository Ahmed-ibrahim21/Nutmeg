package com.wr.nutmeg.finance;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.exceptions.InvalidArgumentsException;
import com.wr.nutmeg.finance.dtos.BalanceResponse;
import com.wr.nutmeg.finance.dtos.FinancialSummary;
import com.wr.nutmeg.finance.dtos.TransactionResponse;
import com.wr.nutmeg.match.Fixture;
import com.wr.nutmeg.player.Player;
import com.wr.nutmeg.player.PlayerRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private FinanceService financeService;

    private Club club;
    private UUID clubId;

    @BeforeEach
    void setUp() {
        clubId = UUID.randomUUID();
        club = new Club();
        club.setId(clubId);
        club.setName("Test FC");
        club.setBalance(1_000_000L);
        club.setStadiumCapacity(20_000);
    }

    @Test
    void creditIncreasesBalanceAndRecordsTransaction() {
        when(clubRepository.findByIdForUpdate(clubId)).thenReturn(Optional.of(club));
        when(transactionRepository.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FinancialTransaction tx = financeService.credit(
                clubId,
                250_000L,
                TransactionType.SPONSORSHIP,
                "Shirt Sponsor",
                null
        );

        assertThat(club.getBalance()).isEqualTo(1_250_000L);
        assertThat(tx.getAmount()).isEqualTo(250_000L);
        assertThat(tx.getBalanceAfter()).isEqualTo(1_250_000L);
        assertThat(tx.getType()).isEqualTo(TransactionType.SPONSORSHIP);
        assertThat(tx.getDescription()).isEqualTo("Shirt Sponsor");

        verify(clubRepository).save(club);
        verify(transactionRepository).save(any(FinancialTransaction.class));
    }

    @Test
    void creditWithZeroOrNegativeThrowsInvalidArguments() {
        assertThatThrownBy(() -> financeService.credit(clubId, 0, TransactionType.PRIZE_MONEY, "test", null))
                .isInstanceOf(InvalidArgumentsException.class);

        assertThatThrownBy(() -> financeService.credit(clubId, -100, TransactionType.PRIZE_MONEY, "test", null))
                .isInstanceOf(InvalidArgumentsException.class);
    }

    @Test
    void debitDecreasesBalanceAndRecordsNegativeAmount() {
        when(clubRepository.findByIdForUpdate(clubId)).thenReturn(Optional.of(club));
        when(transactionRepository.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FinancialTransaction tx = financeService.debit(
                clubId,
                400_000L,
                TransactionType.STADIUM_UPGRADE,
                "North Stand expansion",
                null
        );

        assertThat(club.getBalance()).isEqualTo(600_000L);
        assertThat(tx.getAmount()).isEqualTo(-400_000L);
        assertThat(tx.getBalanceAfter()).isEqualTo(600_000L);
        assertThat(tx.getType()).isEqualTo(TransactionType.STADIUM_UPGRADE);

        verify(clubRepository).save(club);
    }

    @Test
    void debitThrowsInsufficientFundsWhenBalanceTooLow() {
        when(clubRepository.findByIdForUpdate(clubId)).thenReturn(Optional.of(club));

        assertThatThrownBy(() -> financeService.debit(
                clubId,
                2_000_000L,
                TransactionType.PLAYER_PURCHASE,
                "Star striker",
                null
        )).isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("insufficient funds");
    }

    @Test
    void getBalanceReturnsCorrectInfo() {
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        BalanceResponse res = financeService.getBalance(clubId);
        assertThat(res.clubId()).isEqualTo(clubId);
        assertThat(res.clubName()).isEqualTo("Test FC");
        assertThat(res.balance()).isEqualTo(1_000_000L);
    }

    @Test
    void processMatchDayRevenueCreditsHomeClub() {
        Fixture fixture = new Fixture();
        fixture.setId(UUID.randomUUID());
        fixture.setHomeClub(club);

        Club awayClub = new Club();
        awayClub.setName("Rival FC");
        fixture.setAwayClub(awayClub);

        when(clubRepository.findByIdForUpdate(clubId)).thenReturn(Optional.of(club));
        when(transactionRepository.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FinancialTransaction tx = financeService.processMatchDayRevenue(fixture);

        assertThat(tx).isNotNull();
        assertThat(tx.getAmount()).isGreaterThan(0);
        assertThat(tx.getType()).isEqualTo(TransactionType.MATCH_DAY_REVENUE);
        assertThat(tx.getDescription()).contains("gate receipts");
        assertThat(club.getBalance()).isGreaterThan(1_000_000L);
    }

    @Test
    void getFinancialSummaryComputesIncomeExpensesAndNet() {
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        FinancialTransaction t1 = new FinancialTransaction();
        t1.setClub(club);
        t1.setType(TransactionType.MATCH_DAY_REVENUE);
        t1.setAmount(50_000L);

        FinancialTransaction t2 = new FinancialTransaction();
        t2.setClub(club);
        t2.setType(TransactionType.WAGE_PAYMENT);
        t2.setAmount(-20_000L);

        when(transactionRepository.findByClubIdOrderByCreatedAtDesc(clubId)).thenReturn(List.of(t1, t2));

        Player player = new Player();
        player.setWeeklyWage(10_000L);
        when(playerRepository.findByClubId(clubId)).thenReturn(List.of(player));

        FinancialSummary summary = financeService.getFinancialSummary(clubId);

        assertThat(summary.balance()).isEqualTo(1_000_000L);
        assertThat(summary.totalIncome()).isEqualTo(50_000L);
        assertThat(summary.totalExpenses()).isEqualTo(20_000L);
        assertThat(summary.netProfitOrLoss()).isEqualTo(30_000L);
        assertThat(summary.weeklyWageBill()).isEqualTo(10_000L);
        assertThat(summary.breakdownByType()).containsEntry(TransactionType.MATCH_DAY_REVENUE, 50_000L);
    }
}
