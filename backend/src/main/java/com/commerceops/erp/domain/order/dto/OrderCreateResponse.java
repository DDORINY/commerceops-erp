package com.commerceops.erp.domain.order.dto;

import com.commerceops.erp.domain.order.entity.Order;

public record OrderCreateResponse(
        Long orderId,
        String orderNumber,
        Integer totalPrice,
        String status,
        String paymentStatus
) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getPaymentStatus().name()
        );
    }
}
