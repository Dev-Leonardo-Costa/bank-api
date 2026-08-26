package com.leonardo.bank_api.pix.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePixScheduleRequest(

        @Schema(
                description = "ID da conta de origem",
                example = "1"
        )
        @NotNull(message = "A conta de origem é obrigatória")
        Long sourceAccountId,

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
        BigDecimal amount,

        @Schema(
                description = "Data e horário para a Transferencia do PIX",
                example = "2026-09-10T14:30:00"
        )
        @NotNull(message = "A data do agendamento é obrigatória")
        @Future(message = "A data do agendamento deve estar no futuro")
        LocalDateTime scheduledAt

) { }