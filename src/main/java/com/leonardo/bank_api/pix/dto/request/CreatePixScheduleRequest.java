package com.leonardo.bank_api.pix.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePixScheduleRequest(

        @NotNull(message = "A conta de origem é obrigatória")
        Long sourceAccountId,

        @NotBlank(message = "A chave PIX é obrigatória")
        String pixKey,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "O valor deve ser maior que zero"
        )
        BigDecimal amount,

        @NotNull(message = "A data do agendamento é obrigatória")
        @Future(message = "A data do agendamento deve estar no futuro")
        LocalDateTime scheduledAt

) { }