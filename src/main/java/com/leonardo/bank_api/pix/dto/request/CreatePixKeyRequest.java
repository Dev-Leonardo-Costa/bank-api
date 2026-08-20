package com.leonardo.bank_api.pix.dto.request;

import com.leonardo.bank_api.shared.enums.PixKeyType;
import jakarta.validation.constraints.NotNull;

public record CreatePixKeyRequest(

        @NotNull(message = "O tipo da chave PIX é obrigatório")
        PixKeyType type,

        String keyValue
) {
}