package com.commerceops.erp.domain.production.dto;

import com.commerceops.erp.domain.production.entity.ProductionOrder;

import java.time.LocalDateTime;
import java.util.List;

public record ProductionOrderResponse(
        Long id,
        String productionNumber,
        String status,
        Long warehouseId,
        String warehouseName,
        Integer plannedQuantity,
        Integer completedQuantity,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String memo,
        Long createdBy,
        Long updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProductionOrderItemResponse> items
) {
    public static ProductionOrderResponse from(ProductionOrder order) {
        return new ProductionOrderResponse(
                order.getId(),
                order.getProductionNumber(),
                order.getStatus().name(),
                order.getWarehouse().getId(),
                order.getWarehouse().getName(),
                order.getPlannedQuantity(),
                order.getCompletedQuantity(),
                order.getStartedAt(),
                order.getCompletedAt(),
                order.getMemo(),
                order.getCreatedBy() != null ? order.getCreatedBy().getId() : null,
                order.getUpdatedBy() != null ? order.getUpdatedBy().getId() : null,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(ProductionOrderItemResponse::from).toList()
        );
    }
}
