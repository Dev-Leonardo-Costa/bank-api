package com.leonardo.bank_api.pix.dto.request;

import com.leonardo.bank_api.shared.enums.PixKeyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreatePixKeyRequest(

        @Schema(
                description = "Tipo da chave PIX",
                example = "CPF",
                allowableValues = {"CPF", "EMAIL", "PHONE", "RANDOM"}
        )
        @NotNull(message = "O tipo da chave PIX é obrigatório")
        PixKeyType type,

        @Schema(
                description = "Valor da chave PIX",
                example = "85999999999"
        )
        String keyValue
) {
}