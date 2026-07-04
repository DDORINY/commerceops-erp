package com.commerceops.erp.domain.inventory.dto;

import com.commerceops.erp.domain.product.entity.Product;

public record InventoryResponse(
        Long productId,
        String productName,
        Integer stockQuantity,
        Integer lowStockThreshold,
        String status
) {
    private static final int LOW_STOCK_THRESHOLD = 5;

    public static InventoryResponse from(Product product) {
        String inventoryStatus;
        if (product.getStockQuantity() <= 0) {
            inventoryStatus = "OUT_OF_STOCK";
        } else if (product.getStockQuantity() <= LOW_STOCK_THRESHOLD) {
            inventoryStatus = "LOW_STOCK";
        } else {
            inventoryStatus = "NORMAL";
        }
        return new InventoryResponse(
                product.getId(),
                product.getName(),
                product.getStockQuantity(),
                LOW_STOCK_THRESHOLD,
                inventoryStatus
        );
    }
}
