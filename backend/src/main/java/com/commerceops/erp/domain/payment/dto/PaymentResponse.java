package com.commerceops.erp.domain.payment.dto;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.payment.entity.Payment;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        String paymentMethod,
        String paymentStatus,
        Integer paidAmount,
        String transactionId,
        String idempotencyKey
) {
    public static PaymentResponse from(Payment payment, Order order) {
        return new PaymentResponse(
                payment.getId(),
                order.getId(),
                payment.getPaymentMethod() == null ? null : payment.getPaymentMethod().name(),
                payment.getPaymentStatus().name(),
                payment.getPaidAmount(),
                payment.getTransactionId(),
                payment.getIdempotencyKey()
        );
    }
}
