package com.commerceops.erp.domain.order.dto;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.payment.entity.Payment;

import java.time.LocalDateTime;

public record AdminOrderResponse(
        Long orderId,
        String orderNumber,
        String userName,
        String userEmail,
        String receiverName,
        Integer totalPrice,
        String status,
        String paymentStatus,
        String paymentProvider,
        String paymentMethod,
        Integer approvedAmount,
        LocalDateTime approvedAt,
        String paymentFailure,
        Long itemCount,
        LocalDateTime createdAt
) {
    public static AdminOrderResponse from(Order order) {
        return from(order, 0L, null);
    }

    public static AdminOrderResponse from(Order order, Long itemCount) {
        return from(order, itemCount, null);
    }

    public static AdminOrderResponse from(Order order, Long itemCount, Payment payment) {
        return new AdminOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getReceiverName(),
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                payment == null ? null : payment.getProvider(),
                payment == null || payment.getPaymentMethod() == null ? null : payment.getPaymentMethod().name(),
                payment == null ? null : payment.getApprovedAmount(),
                payment == null ? null : payment.getApprovedAt(),
                payment == null ? null : payment.getFailureMessage(),
                itemCount == null ? 0L : itemCount,
                order.getCreatedAt()
        );
    }
}
