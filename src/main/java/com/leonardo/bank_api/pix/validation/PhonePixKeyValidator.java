package com.leonardo.bank_api.pix.validation;

import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.shared.enums.PixKeyType;
import org.springframework.stereotype.Component;

@Component
public class PhonePixKeyValidator implements PixKeyValidator {

    @Override
    public PixKeyType getType() {
        return PixKeyType.PHONE;
    }

    @Override
    public String validateAndNormalize(String keyValue) {

        if (keyValue == null || keyValue.isBlank()) {
            throw new BusinessException(
                    "Telefone da chave PIX é obrigatório"
            );
        }

        String normalized = keyValue.replaceAll("\\D", "");

        if (normalized.length() != 11) {
            throw new BusinessException(
                    "Telefone da chave PIX inválido"
            );
        }

        if (!normalized.matches("[1-9]{2}9[0-9]{8}")) {
            throw new BusinessException(
                    "Telefone da chave PIX inválido"
            );
        }

        return "+55" + normalized;
    }
}