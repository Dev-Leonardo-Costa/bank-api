package com.leonardo.bank_api.pix.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdatePixLimitRequest(

        @Schema(
                description = "Limite diário PIX",
                example = "5000.00",
                minimum = "0.01"
        )
        @NotNull(message = "O limite diário PIX é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "O limite diário PIX deve ser maior que zero"
        )
        BigDecimal dailyPixLimit

) { }