package com.commerceops.erp.domain.barcode.dto;

import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;

import java.util.List;

public record BarcodeStockResponse(
        Long skuId,
        String skuCode,
        String barcode,
        String skuName,
        Long productId,
        String productName,
        String productCode,
        Integer productStockQuantity,
        Integer safetyStockQuantity,
        Boolean active,
        List<BarcodeWarehouseStockResponse> warehouseStocks
) {
    public static BarcodeStockResponse from(Sku sku, List<WarehouseStock> stocks) {
        return new BarcodeStockResponse(
                sku.getId(),
                sku.getSkuCode(),
                sku.getBarcode(),
                sku.getName(),
                sku.getProduct().getId(),
                sku.getProduct().getName(),
                sku.getProduct().getProductCode(),
                sku.getProduct().getStockQuantity(),
                sku.getSafetyStockQuantity(),
                sku.getActive(),
                stocks.stream().map(BarcodeWarehouseStockResponse::from).toList()
        );
    }
}
