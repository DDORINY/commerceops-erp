package com.commerceops.erp.domain.payment.dto;

import com.commerceops.erp.domain.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentApproveRequest(
        @NotNull Long orderId,
        @NotNull PaymentMethod paymentMethod,
        String providerTransactionId,
        String idempotencyKey
) {
}
