package com.leonardo.bank_api.pix.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PixTransferRequest(


        @Schema(
                description = "Chave PIX da conta de destino",
                example = "85999999999"
        )
        @NotBlank(message = "A chave PIX é obrigatória")
        String pixKey,

        @Schema(
                description = "Valor da operação",
                example = "250.00",
                minimum = "0.01"
        )
        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "O valor deve ser maior que zero"
        )
        BigDecimal amount

) { }