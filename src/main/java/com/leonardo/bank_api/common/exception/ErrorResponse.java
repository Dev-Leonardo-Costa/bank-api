package com.leonardo.bank_api.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(

        @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss")
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<ErrorField> details

) { }
