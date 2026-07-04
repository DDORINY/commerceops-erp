package com.commerceops.erp.domain.dashboard.dto;

import com.commerceops.erp.domain.product.entity.Product;

public record LowStockProductResponse(
        Long productId,
        String productName,
        Integer stockQuantity,
        Integer lowStockThreshold
) {
    private static final int LOW_STOCK_THRESHOLD = 5;

    public static LowStockProductResponse from(Product product) {
        return new LowStockProductResponse(
                product.getId(),
                product.getName(),
                product.getStockQuantity(),
                LOW_STOCK_THRESHOLD
        );
    }
}
