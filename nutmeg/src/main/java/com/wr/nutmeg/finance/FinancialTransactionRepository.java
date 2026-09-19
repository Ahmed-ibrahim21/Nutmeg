package com.wr.nutmeg.finance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    List<FinancialTransaction> findByClubIdOrderByCreatedAtDesc(UUID clubId);

    List<FinancialTransaction> findByClubIdAndTypeOrderByCreatedAtDesc(UUID clubId, TransactionType type);
}
