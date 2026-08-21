package com.leonardo.bank_api.transaction.repository;

import com.leonardo.bank_api.shared.enums.TransactionStatus;
import com.leonardo.bank_api.shared.enums.TransactionType;
import com.leonardo.bank_api.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.type = :type
          AND t.status = :status
          AND t.sourceAccount.id = :accountId
          AND t.createdAt >= :startOfDay
          AND t.createdAt < :endOfDay
        """)
    BigDecimal sumAmountByAccountAndPeriod(
            @Param("accountId") Long accountId,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT t
        FROM Transaction t
        WHERE t.sourceAccount.id = :accountId
           OR t.destinationAccount.id = :accountId
        ORDER BY t.createdAt DESC
        """)
    Page<Transaction> findStatementByAccountId(@Param("accountId") Long accountId, Pageable pageable);



}