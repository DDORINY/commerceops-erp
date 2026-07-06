package com.commerceops.erp.domain.warehouse.dto;

import com.commerceops.erp.domain.warehouse.entity.WarehouseLocation;

import java.time.LocalDateTime;

public record WarehouseLocationResponse(
        Long locationId,
        Long warehouseId,
        String warehouseCode,
        String warehouseName,
        String code,
        String name,
        String zone,
        String aisle,
        String rack,
        String cell,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WarehouseLocationResponse from(WarehouseLocation location) {
        return new WarehouseLocationResponse(
                location.getId(),
                location.getWarehouse().getId(),
                location.getWarehouse().getCode(),
                location.getWarehouse().getName(),
                location.getCode(),
                location.getName(),
                location.getZone(),
                location.getAisle(),
                location.getRack(),
                location.getCell(),
                location.isActive(),
                location.getCreatedAt(),
                location.getUpdatedAt()
        );
    }
}
