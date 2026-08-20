package com.leonardo.bank_api.pix.validation;

import com.leonardo.bank_api.shared.enums.PixKeyType;

public interface PixKeyValidator {

    PixKeyType getType();

    String validateAndNormalize(String keyValue);
}