package com.commerceops.erp.domain.cart.dto;

import com.commerceops.erp.domain.cart.entity.Cart;

public record CartItemResponse(
        Long cartId,
        Long productId,
        String productName,
        Integer price,
        Integer quantity,
        Integer stockQuantity,
        String imageUrl,
        Integer subtotal,
        String selectedOptions
) {
    public static CartItemResponse from(Cart cart) {
        int subtotal = cart.getProduct().getPrice() * cart.getQuantity();
        return new CartItemResponse(
                cart.getId(),
                cart.getProduct().getId(),
                cart.getProduct().getName(),
                cart.getProduct().getPrice(),
                cart.getQuantity(),
                cart.getProduct().getStockQuantity(),
                cart.getProduct().getImageUrl(),
                subtotal,
                cart.getSelectedOptions()
        );
    }
}
