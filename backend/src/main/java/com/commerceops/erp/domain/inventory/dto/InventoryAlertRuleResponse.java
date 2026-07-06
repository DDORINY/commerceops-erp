package com.commerceops.erp.domain.inventory.dto;

import com.commerceops.erp.domain.inventory.entity.InventoryAlertRule;

import java.time.LocalDateTime;

public record InventoryAlertRuleResponse(
        Long ruleId,
        Long skuId,
        String skuCode,
        String barcode,
        Long productId,
        String productName,
        Long warehouseId,
        String warehouseName,
        int thresholdQuantity,
        boolean active,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InventoryAlertRuleResponse from(InventoryAlertRule rule) {
        return new InventoryAlertRuleResponse(
                rule.getId(),
                rule.getSku().getId(),
                rule.getSku().getSkuCode(),
                rule.getSku().getBarcode(),
                rule.getSku().getProduct().getId(),
                rule.getSku().getProduct().getName(),
                rule.getWarehouse() == null ? null : rule.getWarehouse().getId(),
                rule.getWarehouse() == null ? "전체 창고" : rule.getWarehouse().getName(),
                rule.getThresholdQuantity(),
                rule.isActive(),
                rule.getMemo(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
