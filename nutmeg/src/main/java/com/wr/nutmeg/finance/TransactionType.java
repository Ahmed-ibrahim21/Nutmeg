package com.wr.nutmeg.finance;

public enum TransactionType {
    // Income
    MATCH_DAY_REVENUE,      // gate receipts from home matches
    WIN_BONUS,              // bonus for winning a match
    DRAW_BONUS,             // smaller bonus for drawing a match
    PRIZE_MONEY,            // league position / cup prize
    PLAYER_SALE,            // transfer market income
    SPONSORSHIP,            // sponsor deals
    COMMERCIAL_REVENUE,     // per-round income based on league standing

    // Expenses
    PLAYER_PURCHASE,        // transfer market spending
    WAGE_PAYMENT,           // weekly squad wages
    STADIUM_UPGRADE,        // stadium capacity / facilities
    TRAINING_FACILITY,      // training ground upgrades
    SCOUT_HIRING,           // hiring scouts
    SCOUT_SALARY,           // recurring scout wages
    OTHER_EXPENSE
}
