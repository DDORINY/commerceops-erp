package com.commerceops.erp.domain.production.dto;

import com.commerceops.erp.domain.production.entity.ProductionReceipt;

import java.time.LocalDateTime;

public record ProductionReceiptResponse(
        Long id,
        Long productionOrderId,
        String productionNumber,
        Long skuId,
        String skuCode,
        String barcode,
        Long productId,
        String productName,
        Long warehouseId,
        String warehouseName,
        Integer quantity,
        Long inventoryLogId,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static ProductionReceiptResponse from(ProductionReceipt receipt) {
        return new ProductionReceiptResponse(
                receipt.getId(),
                receipt.getProductionOrder().getId(),
                receipt.getProductionOrder().getProductionNumber(),
                receipt.getSku().getId(),
                receipt.getSku().getSkuCode(),
                receipt.getSku().getBarcode(),
                receipt.getProduct().getId(),
                receipt.getProduct().getName(),
                receipt.getWarehouse().getId(),
                receipt.getWarehouse().getName(),
                receipt.getQuantity(),
                receipt.getInventoryLogId(),
                receipt.getCreatedBy() != null ? receipt.getCreatedBy().getId() : null,
                receipt.getCreatedAt()
        );
    }
}
