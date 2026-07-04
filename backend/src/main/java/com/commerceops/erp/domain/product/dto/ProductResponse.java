package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.entity.Product;

import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        Integer price,
        Integer stockQuantity,
        String imageUrl,
        String status,
        List<ProductOptionGroup> options,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getStatus().name(),
                product.getOptions() != null ? product.getOptions() : List.of(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
