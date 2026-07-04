package com.commerceops.erp.domain.coupon.dto;

import com.commerceops.erp.domain.coupon.enums.DiscountType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CouponCreateRequest(
        @NotBlank(message = "쿠폰 코드는 필수입니다.")
        String code,

        @NotNull(message = "할인 유형은 필수입니다.")
        DiscountType discountType,

        @NotNull @Min(1)
        Integer discountValue,

        @NotNull @Min(0)
        Integer minOrderAmount,

        @NotNull @Min(1)
        Integer maxUsage,

        @NotNull
        LocalDateTime expiresAt
) {}
