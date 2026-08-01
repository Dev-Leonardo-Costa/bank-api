package com.leonardo.bank_api.common.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(

        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<ErrorField> details

) { }
