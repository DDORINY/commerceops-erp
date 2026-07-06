package com.commerceops.erp.domain.production.dto;

import com.commerceops.erp.domain.production.entity.ProductionOrder;

import java.time.LocalDateTime;

public record ProductionOrderListResponse(
        Long id,
        String productionNumber,
        String status,
        Long warehouseId,
        String warehouseName,
        Integer plannedQuantity,
        Integer completedQuantity,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {
    public static ProductionOrderListResponse from(ProductionOrder order) {
        return new ProductionOrderListResponse(
                order.getId(),
                order.getProductionNumber(),
                order.getStatus().name(),
                order.getWarehouse().getId(),
                order.getWarehouse().getName(),
                order.getPlannedQuantity(),
                order.getCompletedQuantity(),
                order.getStartedAt(),
                order.getCompletedAt(),
                order.getCreatedAt()
        );
    }
}
