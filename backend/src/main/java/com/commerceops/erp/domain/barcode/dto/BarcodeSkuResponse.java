package com.commerceops.erp.domain.barcode.dto;

import com.commerceops.erp.domain.sku.entity.Sku;

public record BarcodeSkuResponse(
        Long skuId,
        String skuCode,
        String barcode,
        String skuName,
        Long productId,
        String productName,
        String productCode,
        Integer stockQuantity,
        Boolean active,
        Integer safetyStockQuantity
) {
    public static BarcodeSkuResponse from(Sku sku) {
        return new BarcodeSkuResponse(
                sku.getId(),
                sku.getSkuCode(),
                sku.getBarcode(),
                sku.getName(),
                sku.getProduct().getId(),
                sku.getProduct().getName(),
                sku.getProduct().getProductCode(),
                sku.getProduct().getStockQuantity(),
                sku.getActive(),
                sku.getSafetyStockQuantity()
        );
    }
}
