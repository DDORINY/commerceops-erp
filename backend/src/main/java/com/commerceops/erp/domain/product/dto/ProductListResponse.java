package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.entity.Product;

import java.time.LocalDateTime;
import java.util.List;

public record ProductListResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        Integer price,
        Integer stockQuantity,
        String imageUrl,
        String status,
        List<ProductOptionGroup> options,
        LocalDateTime createdAt
) {
    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getStatus().name(),
                product.getOptions() != null ? product.getOptions() : List.of(),
                product.getCreatedAt()
        );
    }
}
