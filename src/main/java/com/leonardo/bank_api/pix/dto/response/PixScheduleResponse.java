package com.leonardo.bank_api.pix.dto.response;


import com.leonardo.bank_api.shared.enums.PixScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PixScheduleResponse(

        Long id,
        Long sourceAccountId,
        String pixKey,
        BigDecimal amount,
        LocalDateTime scheduledAt,
        PixScheduleStatus status,
        LocalDateTime createdAt

) { }