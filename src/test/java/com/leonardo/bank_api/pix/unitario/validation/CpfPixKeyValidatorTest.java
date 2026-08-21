package com.leonardo.bank_api.pix.unitario.validation;

import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.pix.validation.CpfPixKeyValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfPixKeyValidatorTest {

    private final CpfPixKeyValidator validator =
            new CpfPixKeyValidator();

    @Test
    void shouldNormalizeValidCpf() {

        String result =
                validator.validateAndNormalize(
                        "529.982.247-25"
                );

        assertThat(result)
                .isEqualTo("52998224725");
    }

    @Test
    void shouldThrowExceptionWhenCpfIsInvalid() {

        assertThatThrownBy(() ->
                validator.validateAndNormalize(
                        "123.456.789-00"
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("CPF da chave PIX inválido");
    }

    @Test
    void shouldThrowExceptionWhenCpfHasRepeatedDigits() {

        assertThatThrownBy(() ->
                validator.validateAndNormalize(
                        "111.111.111-11"
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("CPF da chave PIX inválido");
    }

    @Test
    void shouldThrowExceptionWhenCpfIsBlank() {

        assertThatThrownBy(() ->
                validator.validateAndNormalize(" ")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("CPF da chave PIX é obrigatório");
    }

    @Test
    void shouldThrowExceptionWhenCpfHasInvalidLength() {

        assertThatThrownBy(() ->
                validator.validateAndNormalize(
                        "123456789"
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("CPF da chave PIX inválido");
    }


}