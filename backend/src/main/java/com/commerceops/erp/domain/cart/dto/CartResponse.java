package com.commerceops.erp.domain.cart.dto;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        Integer totalPrice,
        Integer totalQuantity
) {
    public static CartResponse of(List<CartItemResponse> items) {
        int totalPrice = items.stream()
                .mapToInt(CartItemResponse::subtotal)
                .sum();
        int totalQuantity = items.stream().mapToInt(CartItemResponse::quantity).sum();
        return new CartResponse(items, totalPrice, totalQuantity);
    }
}
