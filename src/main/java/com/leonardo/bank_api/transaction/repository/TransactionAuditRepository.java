package com.leonardo.bank_api.transaction.repository;

import com.leonardo.bank_api.transaction.entity.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {
}