package com.leonardo.bank_api.pix.dto.response;

import com.leonardo.bank_api.shared.enums.PixKeyType;

public record PixRecipientResponse(

        String name,
        PixKeyType keyType,
        String keyValue,
        String bank

) { }