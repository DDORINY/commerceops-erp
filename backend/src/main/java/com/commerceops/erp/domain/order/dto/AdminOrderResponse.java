package com.commerceops.erp.domain.order.dto;

import com.commerceops.erp.domain.order.entity.Order;

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
        Long itemCount,
        LocalDateTime createdAt
) {
    public static AdminOrderResponse from(Order order) {
        return from(order, 0L);
    }

    public static AdminOrderResponse from(Order order, Long itemCount) {
        return new AdminOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getReceiverName(),
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                itemCount == null ? 0L : itemCount,
                order.getCreatedAt()
        );
    }
}
