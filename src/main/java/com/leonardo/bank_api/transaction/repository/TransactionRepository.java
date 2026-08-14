package com.leonardo.bank_api.transaction.repository;

import com.leonardo.bank_api.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}