package com.commerceops.erp.domain.production.dto;

import com.commerceops.erp.domain.production.entity.ProductionOrderItem;

public record ProductionOrderItemResponse(
        Long id,
        Long skuId,
        String skuCode,
        String barcode,
        Long productId,
        String productName,
        String productCode,
        Integer plannedQuantity,
        Integer completedQuantity
) {
    public static ProductionOrderItemResponse from(ProductionOrderItem item) {
        return new ProductionOrderItemResponse(
                item.getId(),
                item.getSku().getId(),
                item.getSku().getSkuCode(),
                item.getSku().getBarcode(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getProductCode(),
                item.getPlannedQuantity(),
                item.getCompletedQuantity()
        );
    }
}
