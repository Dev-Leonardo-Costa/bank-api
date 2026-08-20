package com.leonardo.bank_api.pix.dto.response;

import com.leonardo.bank_api.shared.enums.PixKeyType;

import java.time.LocalDateTime;

public record PixKeyResponse(

        Long id,
        PixKeyType type,
        String keyValue,
        Long accountId,
        LocalDateTime createdAt

) {
}