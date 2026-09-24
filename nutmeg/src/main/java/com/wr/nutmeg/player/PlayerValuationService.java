package com.wr.nutmeg.player;

import com.wr.nutmeg.common.enums.PlayerRole;
import org.springframework.stereotype.Service;

/**
 * Centralised player valuation engine.
 * <p>
 * Market value is derived from: overall rating, age curve, potential ceiling,
 * contract remaining years, and a position demand multiplier.
 * <p>
 * Weekly wage is derived from market value with a position-demand premium,
 * keeping a sensible ratio that drains club finances meaningfully.
 */
@Service
public class PlayerValuationService {

    // ── Market-value constants ──────────────────────────────────────────────

    /** Base multiplier applied to (overall²). Higher = richer economy. */
    private static final long BASE_VALUE_MULTIPLIER = 1_200L;

    /** Age that gives peak market value. */
    private static final int PEAK_AGE = 27;

    /** Maximum bonus / malus from age curve (±30 %). */
    private static final double MAX_AGE_FACTOR_DELTA = 0.30;

    /** How much unrealised potential adds to value (per point of headroom). */
    private static final double POTENTIAL_HEADROOM_FACTOR = 0.015;

    /** Minimum contract-remaining multiplier (< 1 year left → penalty). */
    private static final double MIN_CONTRACT_FACTOR = 0.60;

    /** Absolute floor for market value so a player is never "free". */
    private static final long MIN_MARKET_VALUE = 10_000L;

    // ── Wage constants ──────────────────────────────────────────────────────

    /**
     * Wage = marketValue / WAGE_DIVISOR.
     * With a 400 divisor an 8 M player earns ~20 k/week; a 1 M player ~2.5 k.
     * Keeps a 20-man squad's wage bill around €200-300k/week — tight but
     * sustainable against matchday + bonus income.
     */
    private static final long WAGE_DIVISOR = 400L;

    /** Absolute floor for weekly wage. */
    private static final long MIN_WEEKLY_WAGE = 500L;

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Computes the market value for a player based on their current state.
     *
     * Formula outline:
     * <pre>
     *   baseValue     = overall² × BASE_VALUE_MULTIPLIER
     *   ageFactor     = bell curve centred on PEAK_AGE
     *   potentialBonus = (potential - overall) × POTENTIAL_HEADROOM_FACTOR
     *   contractFactor = lerp(MIN_CONTRACT_FACTOR .. 1.0) over remaining years
     *   positionFactor = demand multiplier for role
     *
     *   marketValue   = baseValue × ageFactor × (1 + potentialBonus)
     *                             × contractFactor × positionFactor
     * </pre>
     */
    public long calculateMarketValue(Player player) {
        int overall = player.getOverallRating();
        long baseValue = (long) overall * overall * BASE_VALUE_MULTIPLIER;

        double ageFactor = computeAgeFactor(player.getAge());
        double potentialBonus = computePotentialBonus(overall, player.getPotential());
        double contractFactor = computeContractFactor(player);
        double positionFactor = computePositionFactor(player.getPosition().getRole());

        long value = Math.round(
                baseValue * ageFactor * (1.0 + potentialBonus)
                        * contractFactor * positionFactor
        );
        return Math.max(MIN_MARKET_VALUE, value);
    }

    /**
     * Computes the weekly wage from a given market value.
     */
    public long calculateWeeklyWage(long marketValue) {
        return Math.max(MIN_WEEKLY_WAGE, marketValue / WAGE_DIVISOR);
    }

    /**
     * Recalculates and persists both marketValue and weeklyWage for a player.
     */
    public void revalue(Player player) {
        long newValue = calculateMarketValue(player);
        player.setMarketValue(newValue);
        player.setWeeklyWage(calculateWeeklyWage(newValue));
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    /**
     * Bell-curve centred on {@link #PEAK_AGE}.
     * Young players (< 22) and old players (> 33) get progressively penalised;
     * prime-age players (24-30) get a bonus.
     */
    private double computeAgeFactor(int age) {
        int distance = Math.abs(age - PEAK_AGE);
        // Every year away from peak costs ~4.3 % (30 % cap at 7 years distance)
        double penalty = Math.min(MAX_AGE_FACTOR_DELTA, distance * (MAX_AGE_FACTOR_DELTA / 7.0));

        // Young players still get a slight premium for upside
        if (age < PEAK_AGE) {
            penalty *= 0.6;  // soften for youth
        }
        return 1.0 + MAX_AGE_FACTOR_DELTA - penalty;
    }

    /**
     * Headroom between current overall and potential. More headroom → higher value.
     */
    private double computePotentialBonus(int overall, int potential) {
        int headroom = Math.max(0, potential - overall);
        return headroom * POTENTIAL_HEADROOM_FACTOR;
    }

    /**
     * Players with expiring contracts are worth less on the market.
     * 3+ years → 1.0;  0 years → {@link #MIN_CONTRACT_FACTOR}.
     */
    private double computeContractFactor(Player player) {
        if (player.getContractExpiry() == null) {
            return 1.0;
        }
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), player.getContractExpiry()
        );
        if (daysRemaining <= 0) {
            return MIN_CONTRACT_FACTOR;
        }
        double yearsRemaining = daysRemaining / 365.0;
        double factor = MIN_CONTRACT_FACTOR
                + (1.0 - MIN_CONTRACT_FACTOR) * Math.min(1.0, yearsRemaining / 3.0);
        return factor;
    }

    /**
     * Position demand multiplier — attackers and goalkeepers command a premium.
     */
    private double computePositionFactor(PlayerRole role) {
        return switch (role) {
            case GK  -> 0.85;   // keepers are niche
            case DEF -> 0.90;
            case MID -> 1.00;
            case FWD -> 1.15;   // goals win games
        };
    }
}
