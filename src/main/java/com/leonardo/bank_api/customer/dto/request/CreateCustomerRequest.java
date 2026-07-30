package com.leonardo.bank_api.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record CreateCustomerRequest(

        @NotBlank(message = "Full name is required")
        String fullName,

        @CPF
        String cpf,
        
        @Email
        String email,

        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) { }
