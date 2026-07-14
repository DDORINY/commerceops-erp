package com.commerceops.erp.domain.payment.dto;

import com.commerceops.erp.domain.payment.entity.Payment;

import java.time.LocalDateTime;

public record AdminPaymentResponse(
        Long paymentId,
        Long orderId,
        String orderNumber,
        Long userId,
        String userName,
        String paymentMethod,
        String paymentStatus,
        Integer paidAmount,
        String transactionId,
        String provider,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminPaymentResponse from(Payment payment) {
        var order = payment.getOrder();
        return new AdminPaymentResponse(
                payment.getId(), order.getId(), order.getOrderNumber(),
                order.getUser().getId(), order.getUser().getName(),
                payment.getPaymentMethod() == null ? null : payment.getPaymentMethod().name(), payment.getPaymentStatus().name(),
                payment.getPaidAmount(), payment.getTransactionId(), payment.getProvider(),
                payment.getCreatedAt(), payment.getUpdatedAt()
        );
    }
}
