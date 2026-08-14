package com.leonardo.bank_api.common.exception;

public class ForbiddenOperationException extends BusinessException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}