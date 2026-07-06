package com.commerceops.erp.domain.barcode.dto;

public record BarcodeStockChangeResponse(
        String barcode,
        Long skuId,
        String skuCode,
        Long productId,
        String productName,
        Long warehouseId,
        String warehouseName,
        Integer quantity,
        Integer beforeProductStock,
        Integer afterProductStock,
        Integer beforeWarehouseStock,
        Integer afterWarehouseStock,
        String type
) {
}
