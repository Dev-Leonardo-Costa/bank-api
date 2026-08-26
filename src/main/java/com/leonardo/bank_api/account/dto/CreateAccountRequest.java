package com.leonardo.bank_api.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

        @Schema(
                description = "ID do cliente para o qual a conta será criada",
                example = "1"
        )
        @NotNull(message = "Customer é obrigatório")
        Long customerId

) {}