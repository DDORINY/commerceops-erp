package com.commerceops.erp.domain.barcode.dto;

public record BarcodeLabelPreviewResponse(
        Long labelId,
        String labelFormat,
        String barcode,
        String skuCode,
        String skuName,
        String productName,
        String html
) {
}
