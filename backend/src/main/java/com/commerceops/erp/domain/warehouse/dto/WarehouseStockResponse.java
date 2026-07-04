package com.commerceops.erp.domain.warehouse.dto;

import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;

public record WarehouseStockResponse(
        Long stockId,
        Long warehouseId,
        String warehouseCode,
        String warehouseName,
        Long productId,
        String productName,
        int quantity,
        int reservedQuantity,
        int availableQuantity,
        int totalProductStock
) {
    public static WarehouseStockResponse from(WarehouseStock stock) {
        return new WarehouseStockResponse(
                stock.getId(), stock.getWarehouse().getId(), stock.getWarehouse().getCode(),
                stock.getWarehouse().getName(), stock.getProduct().getId(), stock.getProduct().getName(),
                stock.getQuantity(), stock.getReservedQuantity(), stock.getAvailableQuantity(),
                stock.getProduct().getStockQuantity()
        );
    }
}
