package com.leonardo.bank_api.pix.validation;

import com.leonardo.bank_api.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhonePixKeyValidatorTest {

    private final PhonePixKeyValidator validator =
            new PhonePixKeyValidator();

    @Test
    void shouldNormalizeValidPhone() {

        String result =
                validator.validateAndNormalize(
                        "(85) 99999-9999"
                );

        assertThat(result)
                .isEqualTo("+5585999999999");
    }

    @Test
    void shouldThrowExceptionWhenPhoneIsBlank() {

        assertThatThrownBy(() ->
                validator.validateAndNormalize(" ")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Telefone da chave PIX é obrigatório");
    }

    @Test
    void shouldThrowExceptionWhenPhoneHasInvalidLength() {

        assertThatThrownBy(() ->
                validator.validateAndNormalize(
                        "859999999"
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Telefone da chave PIX inválido");
    }

    @Test
    void shouldThrowExceptionWhenPhoneDoesNotStartWithNine() {

        assertThatThrownBy(() ->
                validator.validateAndNormalize(
                        "(85) 89999-9999"
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Telefone da chave PIX inválido");
    }

    @Test
    void shouldAcceptPhoneWithoutMask() {

        String result =
                validator.validateAndNormalize(
                        "85999999999"
                );

        assertThat(result)
                .isEqualTo("+5585999999999");
    }
}