package com.commerceops.erp.domain.auth.dto;

public record SignupResponse(
        Long userId,
        String email,
        String name,
        String role
) {}
