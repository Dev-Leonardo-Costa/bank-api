package com.leonardo.bank_api.account.dto;

import com.leonardo.bank_api.shared.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(

        Long id,
        String number,
        String agency,
        BigDecimal balance,
        AccountStatus status,
        Long customerId,
        LocalDateTime createdAt

) {}