package com.commerceops.erp.domain.barcode.dto;

import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;

public record BarcodeWarehouseStockResponse(
        Long warehouseId,
        String warehouseCode,
        String warehouseName,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity
) {
    public static BarcodeWarehouseStockResponse from(WarehouseStock stock) {
        return new BarcodeWarehouseStockResponse(
                stock.getWarehouse().getId(),
                stock.getWarehouse().getCode(),
                stock.getWarehouse().getName(),
                stock.getQuantity(),
                stock.getReservedQuantity(),
                stock.getAvailableQuantity()
        );
    }
}
