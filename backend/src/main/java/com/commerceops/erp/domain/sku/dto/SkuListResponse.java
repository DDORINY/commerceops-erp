package com.commerceops.erp.domain.sku.dto;

import com.commerceops.erp.domain.sku.entity.Sku;

public record SkuListResponse(
        Long id,
        Long productId,
        String productName,
        String productCode,
        String optionSignature,
        String skuCode,
        String barcode,
        String name,
        Integer safetyStockQuantity,
        Boolean active
) {
    public static SkuListResponse from(Sku sku) {
        return new SkuListResponse(
                sku.getId(),
                sku.getProduct().getId(),
                sku.getProduct().getName(),
                sku.getProduct().getProductCode(),
                sku.getOptionSignature(),
                sku.getSkuCode(),
                sku.getBarcode(),
                sku.getName(),
                sku.getSafetyStockQuantity(),
                sku.getActive()
        );
    }
}
