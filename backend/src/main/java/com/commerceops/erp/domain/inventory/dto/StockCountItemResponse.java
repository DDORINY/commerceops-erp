package com.commerceops.erp.domain.inventory.dto;

import com.commerceops.erp.domain.inventory.entity.StockCountItem;

public record StockCountItemResponse(
        Long itemId,
        Long skuId,
        String skuCode,
        String barcode,
        Long productId,
        String productName,
        Integer systemQuantity,
        Integer countedQuantity,
        Integer differenceQuantity,
        String memo
) {
    public static StockCountItemResponse from(StockCountItem item) {
        return new StockCountItemResponse(
                item.getId(),
                item.getSku().getId(),
                item.getSku().getSkuCode(),
                item.getSku().getBarcode(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getSystemQuantity(),
                item.getCountedQuantity(),
                item.getDifferenceQuantity(),
                item.getMemo()
        );
    }
}
