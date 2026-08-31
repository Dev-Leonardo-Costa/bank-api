package com.leonardo.bank_api.transaction.service;

import com.leonardo.bank_api.shared.enums.TransactionAuditAction;
import com.leonardo.bank_api.shared.enums.TransactionAuditStatus;
import com.leonardo.bank_api.transaction.entity.Transaction;

public interface TransactionAuditService {

    void register(
            Transaction transaction,
            TransactionAuditAction action,
            TransactionAuditStatus status,
            String performedBy,
            String details
    );

    void registerFailure(
            TransactionAuditAction action,
            TransactionAuditStatus status,
            String performedBy,
            String details
    );

}