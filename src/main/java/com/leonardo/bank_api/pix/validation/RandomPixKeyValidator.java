package com.leonardo.bank_api.pix.validation;

import com.leonardo.bank_api.shared.enums.PixKeyType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RandomPixKeyValidator implements PixKeyValidator {

    @Override
    public PixKeyType getType() {
        return PixKeyType.RANDOM;
    }

    @Override
    public String validateAndNormalize(String keyValue) {
        return UUID.randomUUID().toString();
    }
}