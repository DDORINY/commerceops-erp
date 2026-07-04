package com.commerceops.erp.domain.warehouse.dto;

import com.commerceops.erp.domain.warehouse.entity.Warehouse;

import java.time.LocalDateTime;

public record WarehouseResponse(
        Long warehouseId,
        String code,
        String name,
        String address,
        boolean active,
        LocalDateTime createdAt
) {
    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(), warehouse.getCode(), warehouse.getName(), warehouse.getAddress(),
                warehouse.isActive(), warehouse.getCreatedAt()
        );
    }
}
