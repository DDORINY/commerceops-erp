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
        String productCode,
        String brand,
        Integer originalPrice,
        Integer discountPrice,
        String tags,
        Integer stockQuantity,
        String imageUrl,
        String status,
        String salesStatus,
        Boolean purchasable,
        String stockDisplayStatus,
        String stockDisplayText,
        Integer remainingStockQuantity,
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
                product.getProductCode(),
                product.getBrand(),
                product.getOriginalPrice(),
                product.getDiscountPrice(),
                product.getTags(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getStatus().name(),
                product.getSalesStatus().name(),
                product.isPurchasable(LocalDateTime.now()),
                product.getStockDisplayStatus().name(),
                product.getStockDisplayText(),
                product.getStockQuantity(),
                product.getOptions() != null ? product.getOptions() : List.of(),
                product.getCreatedAt()
        );
    }
}
