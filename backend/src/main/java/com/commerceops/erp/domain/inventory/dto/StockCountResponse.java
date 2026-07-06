package com.commerceops.erp.domain.inventory.dto;

import com.commerceops.erp.domain.inventory.entity.StockCountSession;
import com.commerceops.erp.domain.inventory.enums.StockCountStatus;

import java.time.LocalDateTime;
import java.util.List;

public record StockCountResponse(
        Long stockCountId,
        String countNumber,
        Long warehouseId,
        String warehouseName,
        StockCountStatus status,
        String memo,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        List<StockCountItemResponse> items
) {
    public static StockCountResponse from(StockCountSession session) {
        return new StockCountResponse(
                session.getId(),
                session.getCountNumber(),
                session.getWarehouse().getId(),
                session.getWarehouse().getName(),
                session.getStatus(),
                session.getMemo(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getCreatedAt(),
                session.getItems().stream()
                        .sorted((left, right) -> left.getId().compareTo(right.getId()))
                        .map(StockCountItemResponse::from)
                        .toList()
        );
    }

    public static StockCountResponse summary(StockCountSession session) {
        return new StockCountResponse(
                session.getId(),
                session.getCountNumber(),
                session.getWarehouse().getId(),
                session.getWarehouse().getName(),
                session.getStatus(),
                session.getMemo(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getCreatedAt(),
                List.of()
        );
    }
}
