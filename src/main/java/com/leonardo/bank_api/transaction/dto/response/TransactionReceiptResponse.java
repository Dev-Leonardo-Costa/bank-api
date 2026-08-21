package com.leonardo.bank_api.transaction.dto.response;

import com.leonardo.bank_api.shared.enums.TransactionStatus;
import com.leonardo.bank_api.shared.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionReceiptResponse(

        Long transactionId,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,

        String payerName,
        String payerAgency,
        String payerAccount,

        String receiverName,
        String receiverAgency,
        String receiverAccount,

        LocalDateTime createdAt

) {
}