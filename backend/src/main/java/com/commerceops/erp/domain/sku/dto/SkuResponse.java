package com.commerceops.erp.domain.sku.dto;

import com.commerceops.erp.domain.sku.entity.Sku;

import java.time.LocalDateTime;

public record SkuResponse(
        Long id,
        Long productId,
        String productName,
        String productCode,
        String optionSignature,
        String skuCode,
        String barcode,
        String name,
        Integer safetyStockQuantity,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SkuResponse from(Sku sku) {
        return new SkuResponse(
                sku.getId(),
                sku.getProduct().getId(),
                sku.getProduct().getName(),
                sku.getProduct().getProductCode(),
                sku.getOptionSignature(),
                sku.getSkuCode(),
                sku.getBarcode(),
                sku.getName(),
                sku.getSafetyStockQuantity(),
                sku.getActive(),
                sku.getCreatedAt(),
                sku.getUpdatedAt()
        );
    }
}
