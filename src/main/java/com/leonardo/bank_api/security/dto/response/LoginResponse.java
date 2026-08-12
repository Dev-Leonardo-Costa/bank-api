package com.leonardo.bank_api.security.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType
) {}