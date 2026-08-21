package com.leonardo.bank_api.pix.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdatePixLimitRequest(

        @NotNull(message = "O limite diário PIX é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "O limite diário PIX deve ser maior que zero"
        )
        BigDecimal dailyPixLimit

) { }