package com.commerceops.erp.domain.review.dto;

import com.commerceops.erp.domain.review.entity.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        Long productId,
        String productName,
        String userName,
        Long orderItemId,
        Integer rating,
        String content,
        String status,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getUser().getName(),
                review.getOrderItemId(),
                review.getRating(),
                review.getContent(),
                review.getEffectiveStatus().name(),
                review.getCreatedAt()
        );
    }
}
