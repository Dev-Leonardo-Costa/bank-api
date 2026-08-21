package com.leonardo.bank_api.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.leonardo.bank_api.shared.enums.TransactionDirection;
import com.leonardo.bank_api.shared.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StatementTransactionResponse(

        Long transactionId,
        TransactionType type,
        TransactionDirection direction,
        BigDecimal amount,
        String description,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt

) { }