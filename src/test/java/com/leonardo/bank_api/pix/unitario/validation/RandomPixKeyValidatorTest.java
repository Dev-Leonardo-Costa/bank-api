package com.leonardo.bank_api.pix.unitario.validation;

import com.leonardo.bank_api.pix.validation.RandomPixKeyValidator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RandomPixKeyValidatorTest {

    private final RandomPixKeyValidator validator =
            new RandomPixKeyValidator();

    @Test
    void shouldGenerateRandomPixKey() {

        String result =
                validator.validateAndNormalize(null);

        assertThat(result)
                .isNotNull()
                .isNotBlank();
    }

    @Test
    void shouldGenerateValidUuid() {

        String result =
                validator.validateAndNormalize(null);

        UUID uuid = UUID.fromString(result);

        assertThat(uuid.toString())
                .isEqualTo(result);
    }

    @Test
    void shouldGenerateDifferentKeys() {

        String firstKey =
                validator.validateAndNormalize(null);

        String secondKey =
                validator.validateAndNormalize(null);

        assertThat(firstKey)
                .isNotEqualTo(secondKey);
    }
}