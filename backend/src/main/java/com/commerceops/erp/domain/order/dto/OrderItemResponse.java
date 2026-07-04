package com.commerceops.erp.domain.order.dto;

import com.commerceops.erp.domain.order.entity.OrderItem;

public record OrderItemResponse(
        Long orderItemId,
        Long productId,
        String productName,
        Integer price,
        Integer quantity,
        Integer subtotal,
        String selectedOptions
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getPrice(),
                item.getQuantity(),
                item.getPrice() * item.getQuantity(),
                item.getSelectedOptions()
        );
    }
}
