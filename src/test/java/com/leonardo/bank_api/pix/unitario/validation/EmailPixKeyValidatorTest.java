package com.leonardo.bank_api.pix.unitario.validation;

import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.pix.validation.EmailPixKeyValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailPixKeyValidatorTest {

    private final EmailPixKeyValidator validator =
            new EmailPixKeyValidator();

    @Test
    void shouldNormalizeValidEmail() {

        String result =
                validator.validateAndNormalize(
                        "  Leonardo@Email.com  "
                );

        assertThat(result)
                .isEqualTo("leonardo@email.com");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {

        assertThatThrownBy(() ->
                validator.validateAndNormalize("email-invalido")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("E-mail da chave PIX inválido");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {

        assertThatThrownBy(() ->
                validator.validateAndNormalize(" ")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("E-mail da chave PIX é obrigatório");
    }
}