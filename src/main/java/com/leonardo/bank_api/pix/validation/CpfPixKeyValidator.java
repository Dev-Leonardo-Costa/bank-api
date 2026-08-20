package com.leonardo.bank_api.pix.validation;

import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.shared.enums.PixKeyType;
import org.springframework.stereotype.Component;

@Component
public class CpfPixKeyValidator implements PixKeyValidator {

    @Override
    public PixKeyType getType() {
        return PixKeyType.CPF;
    }

    @Override
    public String validateAndNormalize(String keyValue) {

        if (keyValue == null || keyValue.isBlank()) {
            throw new BusinessException(
                    "CPF da chave PIX é obrigatório"
            );
        }

        String normalized = keyValue.replaceAll("\\D", "");

        if (normalized.length() != 11) {
            throw new BusinessException(
                    "CPF da chave PIX inválido"
            );
        }

        if (normalized.chars().distinct().count() == 1) {
            throw new BusinessException(
                    "CPF da chave PIX inválido"
            );
        }

        if (!isValidCpf(normalized)) {
            throw new BusinessException(
                    "CPF da chave PIX inválido"
            );
        }

        return normalized;
    }

    private boolean isValidCpf(String cpf) {

        int firstDigit = calculateDigit(cpf, 9, 10);
        int secondDigit = calculateDigit(cpf, 10, 11);

        return firstDigit == Character.getNumericValue(cpf.charAt(9))
                && secondDigit == Character.getNumericValue(cpf.charAt(10));
    }

    private int calculateDigit(
            String cpf,
            int length,
            int weight
    ) {

        int sum = 0;

        for (int i = 0; i < length; i++) {
            int digit = Character.getNumericValue(cpf.charAt(i));

            sum += digit * (weight - i);
        }

        int result = 11 - (sum % 11);

        return result >= 10 ? 0 : result;
    }
}