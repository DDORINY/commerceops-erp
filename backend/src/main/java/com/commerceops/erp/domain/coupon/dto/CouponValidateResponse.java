package com.commerceops.erp.domain.coupon.dto;

public record CouponValidateResponse(
        String code,
        String discountType,
        Integer discountValue,
        Integer discountAmount,
        Integer minOrderAmount
) {}
