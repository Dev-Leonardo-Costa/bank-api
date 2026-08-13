package com.leonardo.bank_api.account.dto;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

        @NotNull(message = "Customer é obrigatório")
        Long customerId

) {}