package com.leonardo.bank_api.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(

        @Schema(
                description = "ID da conta de origem",
                example = "1"
        )
        @NotNull(message = "Conta de destino é obrigatória")
        Long destinationAccountId,

        @Schema(
                description = "Valor da operação",
                example = "250.00",
                minimum = "0.01"
        )
        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal amount

) { }