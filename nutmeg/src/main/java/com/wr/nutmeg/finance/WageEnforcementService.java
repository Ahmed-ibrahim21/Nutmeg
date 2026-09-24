package com.wr.nutmeg.finance;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.player.Player;
import com.wr.nutmeg.player.PlayerRepository;
import com.wr.nutmeg.player.PlayerValuationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Orchestrates wage-related constraints and periodic financial operations.
 * <ul>
 *     <li><b>Automatic wage processing</b> – deducts wages for every club
 *         in a league at the end of a round.</li>
 *     <li><b>Revaluation</b> – recalculates market values and wages for
 *         all players in a league (called periodically).</li>
 * </ul>
 */
@Service
@Slf4j
public class WageEnforcementService {

    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;
    private final PlayerValuationService valuationService;
    private final FinanceService financeService;

    public WageEnforcementService(
            ClubRepository clubRepository,
            PlayerRepository playerRepository,
            PlayerValuationService valuationService,
            FinanceService financeService
    ) {
        this.clubRepository = clubRepository;
        this.playerRepository = playerRepository;
        this.valuationService = valuationService;
        this.financeService = financeService;
    }



    // ── Periodic processing ─────────────────────────────────────────────────

    /**
     * Processes weekly wages for ALL clubs in a league.
     * Should be called once per round (a round ≈ a match-week).
     *
     * @param leagueId the league whose clubs should be charged
     * @return the number of clubs that were charged
     */
    @Transactional
    public int processLeagueWages(UUID leagueId) {
        List<Club> clubs = clubRepository.findByLeagueId(leagueId);
        int processed = 0;
        for (Club club : clubs) {
            try {
                FinancialTransaction tx = financeService.processWeeklyWages(club.getId());
                if (tx != null) {
                    processed++;
                }
            } catch (InsufficientFundsException e) {
                log.warn("Club {} cannot pay wages – insufficient funds (balance: {})",
                        club.getName(), club.getBalance());
                // In a future iteration we could apply morale penalties, force player sales, etc.
            }
        }
        log.info("Processed wages for {}/{} clubs in league {}", processed, clubs.size(), leagueId);
        return processed;
    }

    // ── Revaluation ─────────────────────────────────────────────────────────

    /**
     * Recalculates the market value and weekly wage of every player
     * in the given league, based on their current attributes.
     *
     * @param leagueId the league to revalue
     * @return total number of players revalued
     */
    @Transactional
    public int revalueLeaguePlayers(UUID leagueId) {
        List<Club> clubs = clubRepository.findByLeagueId(leagueId);
        int count = 0;
        for (Club club : clubs) {
            List<Player> squad = playerRepository.findByClubId(club.getId());
            for (Player player : squad) {
                valuationService.revalue(player);
            }
            playerRepository.saveAll(squad);
            count += squad.size();
        }
        log.info("Revalued {} players across {} clubs in league {}", count, clubs.size(), leagueId);
        return count;
    }

    /**
     * Recalculates the market value and weekly wage of every player
     * in the given club, based on their current attributes.
     *
     * @param clubId the club to revalue
     * @return total number of players revalued
     */
    @Transactional
    public int revalueClubPlayers(UUID clubId) {
        List<Player> squad = playerRepository.findByClubId(clubId);
        for (Player player : squad) {
            valuationService.revalue(player);
        }
        playerRepository.saveAll(squad);
        log.info("Revalued {} players for club {}", squad.size(), clubId);
        return squad.size();
    }
}
