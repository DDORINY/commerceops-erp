package com.commerceops.erp.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        UserInfo user
) {
    public record UserInfo(Long id, String email, String name, String role) {}
}
