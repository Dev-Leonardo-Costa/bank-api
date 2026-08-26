package com.leonardo.bank_api.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

@Schema(description = "Dados para cadastro de um novo cliente")
public record CreateCustomerRequest(

        @Schema(description = "Nome completo do cliente",
                example = "João da Silva"
        )
        @NotBlank(message = "Full name is required")
        String fullName,

        @Schema(description = "CPF do cliente",
                example = "123.456.789-00"
        )
        @CPF
        String cpf,

        @Schema(
                description = "E-mail do cliente",
                example = "joao.silva@bankapi.com"
        )
        @Email
        String email,

        @Schema(
                description = "Senha do cliente",
                example = "senhaSegura123"
        )
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) { }
