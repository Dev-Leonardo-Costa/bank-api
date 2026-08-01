package com.leonardo.bank_api.common.exception;

public record ErrorField(

        String field,
        String message

) { }
