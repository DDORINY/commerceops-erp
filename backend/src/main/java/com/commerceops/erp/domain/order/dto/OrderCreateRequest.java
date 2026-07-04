package com.commerceops.erp.domain.order.dto;

import com.commerceops.erp.domain.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotBlank String receiverName,
        @NotBlank String receiverPhone,
        @NotBlank String address,
        String detailAddress,
        @NotNull PaymentMethod paymentMethod,
        @NotEmpty List<Long> cartItemIds,
        String couponCode
) {}
