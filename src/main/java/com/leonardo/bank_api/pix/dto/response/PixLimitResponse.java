package com.leonardo.bank_api.pix.dto.response;

import java.math.BigDecimal;

public record PixLimitResponse(
        BigDecimal dailyPixLimit
) {
}