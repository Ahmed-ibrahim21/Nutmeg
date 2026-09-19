package com.wr.nutmeg.finance.dtos;

import com.wr.nutmeg.finance.TransactionType;

import java.util.Map;

public record FinancialSummary(
        long balance,
        long totalIncome,
        long totalExpenses,
        long netProfitOrLoss,
        long weeklyWageBill,
        Map<TransactionType, Long> breakdownByType
) {}
