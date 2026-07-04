package com.commerceops.erp.domain.auth.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
}
