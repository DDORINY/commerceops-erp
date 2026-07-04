package com.commerceops.erp.domain.wishlist.dto;

import com.commerceops.erp.domain.wishlist.entity.Wishlist;

import java.time.LocalDateTime;

public record WishlistItemResponse(
        Long wishlistId,
        Long productId,
        String productName,
        Integer price,
        String imageUrl,
        String categoryName,
        String status,
        LocalDateTime likedAt
) {
    public static WishlistItemResponse from(Wishlist w) {
        return new WishlistItemResponse(
                w.getId(),
                w.getProduct().getId(),
                w.getProduct().getName(),
                w.getProduct().getPrice(),
                w.getProduct().getImageUrl(),
                w.getProduct().getCategory().getName(),
                w.getProduct().getStatus().name(),
                w.getCreatedAt()
        );
    }
}
