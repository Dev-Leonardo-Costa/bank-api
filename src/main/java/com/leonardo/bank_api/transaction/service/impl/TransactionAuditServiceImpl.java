package com.leonardo.bank_api.transaction.service.impl;

import com.leonardo.bank_api.shared.enums.TransactionAuditAction;
import com.leonardo.bank_api.shared.enums.TransactionAuditStatus;
import com.leonardo.bank_api.transaction.entity.Transaction;
import com.leonardo.bank_api.transaction.entity.TransactionAudit;
import com.leonardo.bank_api.transaction.repository.TransactionAuditRepository;
import com.leonardo.bank_api.transaction.service.TransactionAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionAuditServiceImpl implements TransactionAuditService {

    private final TransactionAuditRepository transactionAuditRepository;

    @Override
    @Transactional
    public void register(
            Transaction transaction,
            TransactionAuditAction action,
            TransactionAuditStatus status,
            String performedBy,
            String details
    ) {

        TransactionAudit audit =
                TransactionAudit.builder()
                        .transaction(transaction)
                        .action(action)
                        .status(status)
                        .performedBy(performedBy)
                        .details(details)
                        .build();

        transactionAuditRepository.save(audit);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailure(
            TransactionAuditAction action,
            TransactionAuditStatus status,
            String performedBy,
            String details
    ) {

        TransactionAudit audit =
                TransactionAudit.builder()
                        .transaction(null)
                        .action(action)
                        .status(status)
                        .performedBy(performedBy)
                        .details(details)
                        .build();

        transactionAuditRepository.save(audit);
    }
}