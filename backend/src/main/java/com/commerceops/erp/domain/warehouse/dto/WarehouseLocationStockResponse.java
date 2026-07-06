package com.commerceops.erp.domain.warehouse.dto;

import com.commerceops.erp.domain.warehouse.entity.WarehouseLocationStock;

import java.time.LocalDateTime;

public record WarehouseLocationStockResponse(
        Long stockId,
        Long locationId,
        String locationCode,
        String locationName,
        Long warehouseId,
        String warehouseName,
        Long skuId,
        String skuCode,
        String barcode,
        Long productId,
        String productName,
        int quantity,
        int reservedQuantity,
        int availableQuantity,
        LocalDateTime updatedAt
) {
    public static WarehouseLocationStockResponse from(WarehouseLocationStock stock) {
        return new WarehouseLocationStockResponse(
                stock.getId(),
                stock.getLocation().getId(),
                stock.getLocation().getCode(),
                stock.getLocation().getName(),
                stock.getWarehouse().getId(),
                stock.getWarehouse().getName(),
                stock.getSku().getId(),
                stock.getSku().getSkuCode(),
                stock.getSku().getBarcode(),
                stock.getProduct().getId(),
                stock.getProduct().getName(),
                stock.getQuantity(),
                stock.getReservedQuantity(),
                stock.getAvailableQuantity(),
                stock.getUpdatedAt()
        );
    }
}
