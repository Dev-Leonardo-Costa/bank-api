package com.leonardo.bank_api.transaction.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.leonardo.bank_api.shared.enums.MovementType;
import com.leonardo.bank_api.shared.enums.TransactionStatus;
import com.leonardo.bank_api.shared.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(

        Long id,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        Long sourceAccountId,
        Long destinationAccountId,
        MovementType movementType,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt

) {
}