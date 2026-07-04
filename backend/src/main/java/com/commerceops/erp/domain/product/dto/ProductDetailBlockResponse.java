package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.entity.ProductDetailBlock;

import java.time.LocalDateTime;

public record ProductDetailBlockResponse(
        Long id,
        String blockType,
        String title,
        String content,
        String imageUrl,
        String specJson,
        Integer sortOrder,
        Boolean visible,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductDetailBlockResponse from(ProductDetailBlock block) {
        return new ProductDetailBlockResponse(
                block.getId(),
                block.getBlockType().name(),
                block.getTitle(),
                block.getContent(),
                block.getImageUrl(),
                block.getSpecJson(),
                block.getSortOrder(),
                block.getVisible(),
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }
}
