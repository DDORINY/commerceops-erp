package com.commerceops.erp.domain.payment.dto;

import com.commerceops.erp.domain.payment.entity.Payment;
import java.time.LocalDateTime;

public record TossPaymentConfirmResponse(
        Long paymentId, Long orderId, String paymentOrderId, String status,
        String method, Integer approvedAmount, LocalDateTime approvedAt
) {
    public static TossPaymentConfirmResponse from(Payment payment) {
        return new TossPaymentConfirmResponse(payment.getId(), payment.getOrder().getId(),
                payment.getProviderOrderId(), payment.getPaymentStatus().name(),
                payment.getPaymentMethod() == null ? null : payment.getPaymentMethod().name(),
                payment.getApprovedAmount(), payment.getApprovedAt());
    }
}
