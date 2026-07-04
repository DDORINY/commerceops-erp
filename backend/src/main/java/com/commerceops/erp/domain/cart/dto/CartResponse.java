package com.commerceops.erp.domain.cart.dto;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        Integer totalPrice
) {
    public static CartResponse of(List<CartItemResponse> items) {
        int totalPrice = items.stream()
                .mapToInt(CartItemResponse::subtotal)
                .sum();
        return new CartResponse(items, totalPrice);
    }
}
