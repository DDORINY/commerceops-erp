package com.commerceops.erp.domain.cart.dto;

import com.commerceops.erp.domain.cart.entity.Cart;

public record CartAddResponse(
        Long cartId,
        Long productId,
        Integer quantity
) {
    public static CartAddResponse from(Cart cart) {
        return new CartAddResponse(cart.getId(), cart.getProduct().getId(), cart.getQuantity());
    }
}
