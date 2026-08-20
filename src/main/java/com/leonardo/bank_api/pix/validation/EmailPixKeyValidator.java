package com.leonardo.bank_api.pix.validation;

import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.shared.enums.PixKeyType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailPixKeyValidator implements PixKeyValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public PixKeyType getType() {
        return PixKeyType.EMAIL;
    }

    @Override
    public String validateAndNormalize(String keyValue) {

        if (keyValue == null || keyValue.isBlank()) {
            throw new BusinessException(
                    "E-mail da chave PIX é obrigatório"
            );
        }

        String normalized = keyValue.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(
                    "E-mail da chave PIX inválido"
            );
        }

        return normalized;
    }
}