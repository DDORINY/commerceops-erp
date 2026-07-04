package com.commerceops.erp.domain.inventory.dto;

import com.commerceops.erp.domain.inventory.entity.InventoryLog;

import java.time.LocalDateTime;

public record InventoryLogResponse(
        Long id,
        Long productId,
        String productName,
        String type,
        Integer quantity,
        Integer beforeStock,
        Integer afterStock,
        String memo,
        LocalDateTime createdAt
) {
    public static InventoryLogResponse from(InventoryLog log) {
        return new InventoryLogResponse(
                log.getId(),
                log.getProduct().getId(),
                log.getProduct().getName(),
                log.getType().name(),
                log.getQuantity(),
                log.getBeforeStock(),
                log.getAfterStock(),
                log.getMemo(),
                log.getCreatedAt()
        );
    }
}
