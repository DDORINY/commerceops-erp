package com.commerceops.erp.domain.outbound.dto;

import com.commerceops.erp.domain.outbound.entity.OutboundOrderItem;

public record OutboundOrderItemResponse(
        Long id,
        Long orderItemId,
        Long skuId,
        String skuCode,
        String barcode,
        Long productId,
        String productName,
        Integer quantity,
        Integer pickedQuantity,
        Integer scannedQuantity
) {
    public static OutboundOrderItemResponse from(OutboundOrderItem item) {
        return new OutboundOrderItemResponse(
                item.getId(),
                item.getOrderItem().getId(),
                item.getSku() != null ? item.getSku().getId() : null,
                item.getSku() != null ? item.getSku().getSkuCode() : null,
                item.getSku() != null ? item.getSku().getBarcode() : null,
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPickedQuantity(),
                item.getScannedQuantity()
        );
    }
}
