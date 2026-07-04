package com.commerceops.erp.domain.coupon.dto;

import com.commerceops.erp.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String code,
        String discountType,
        Integer discountValue,
        Integer minOrderAmount,
        Integer maxUsage,
        Integer usedCount,
        LocalDateTime expiresAt,
        boolean active,
        LocalDateTime createdAt
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getMaxUsage(),
                coupon.getUsedCount(),
                coupon.getExpiresAt(),
                coupon.isActive(),
                coupon.getCreatedAt()
        );
    }
}
