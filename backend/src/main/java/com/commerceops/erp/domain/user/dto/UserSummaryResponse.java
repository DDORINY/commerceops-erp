package com.commerceops.erp.domain.user.dto;

import com.commerceops.erp.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserSummaryResponse(
        Long id,
        String email,
        String name,
        String phone,
        String role,
        String status,
        LocalDateTime createdAt,
        Long orderCount,
        Long totalOrderAmount
) {
    public static UserSummaryResponse from(User user) {
        return from(user, 0L, 0L);
    }

    public static UserSummaryResponse from(User user, Long orderCount, Long totalOrderAmount) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                orderCount == null ? 0L : orderCount,
                totalOrderAmount == null ? 0L : totalOrderAmount
        );
    }
}
