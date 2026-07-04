package com.commerceops.erp.domain.auth.dto;

public record MeResponse(
        Long id,
        String email,
        String name,
        String phone,
        String role,
        String status
) {}
