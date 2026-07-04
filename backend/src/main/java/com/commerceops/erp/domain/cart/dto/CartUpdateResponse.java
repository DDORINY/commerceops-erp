package com.commerceops.erp.domain.cart.dto;

import com.commerceops.erp.domain.cart.entity.Cart;

public record CartUpdateResponse(Long cartId, Integer quantity) {

    public static CartUpdateResponse from(Cart cart) {
        return new CartUpdateResponse(cart.getId(), cart.getQuantity());
    }
}
