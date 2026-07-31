package com.leonardo.bank_api.customer.dto.response;

import java.time.LocalDate;

public record CustomerResponse(

        Long id,
        String fullName,
        String cpf,
        String email,
        LocalDate createdAt

) { }
