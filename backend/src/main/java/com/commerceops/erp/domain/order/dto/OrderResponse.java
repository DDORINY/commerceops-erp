package com.commerceops.erp.domain.order.dto;

import com.commerceops.erp.domain.order.entity.Order;

import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        String orderNumber,
        Integer totalPrice,
        String status,
        String paymentStatus,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                order.getCreatedAt()
        );
    }
}
