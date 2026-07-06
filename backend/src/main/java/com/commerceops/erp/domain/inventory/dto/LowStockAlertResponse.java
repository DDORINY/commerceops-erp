package com.commerceops.erp.domain.inventory.dto;

import com.commerceops.erp.domain.inventory.entity.InventoryAlertRule;

public record LowStockAlertResponse(
        Long ruleId,
        Long skuId,
        String skuCode,
        String barcode,
        Long productId,
        String productName,
        Long warehouseId,
        String warehouseName,
        int currentQuantity,
        int thresholdQuantity,
        int shortageQuantity,
        String memo
) {
    public static LowStockAlertResponse from(InventoryAlertRule rule, int currentQuantity) {
        return new LowStockAlertResponse(
                rule.getId(),
                rule.getSku().getId(),
                rule.getSku().getSkuCode(),
                rule.getSku().getBarcode(),
                rule.getSku().getProduct().getId(),
                rule.getSku().getProduct().getName(),
                rule.getWarehouse() == null ? null : rule.getWarehouse().getId(),
                rule.getWarehouse() == null ? "전체 창고" : rule.getWarehouse().getName(),
                currentQuantity,
                rule.getThresholdQuantity(),
                Math.max(rule.getThresholdQuantity() - currentQuantity, 0),
                rule.getMemo()
        );
    }
}
