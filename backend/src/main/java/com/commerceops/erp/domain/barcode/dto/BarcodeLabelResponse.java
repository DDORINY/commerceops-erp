package com.commerceops.erp.domain.barcode.dto;

import com.commerceops.erp.domain.barcode.entity.BarcodeLabel;

import java.time.LocalDateTime;

public record BarcodeLabelResponse(
        Long id,
        Long skuId,
        String skuCode,
        String barcode,
        String productName,
        String labelFormat,
        Integer printCount,
        LocalDateTime lastPrintedAt,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static BarcodeLabelResponse from(BarcodeLabel label) {
        return new BarcodeLabelResponse(
                label.getId(),
                label.getSku().getId(),
                label.getSku().getSkuCode(),
                label.getBarcode(),
                label.getSku().getProduct().getName(),
                label.getLabelFormat(),
                label.getPrintCount(),
                label.getLastPrintedAt(),
                label.getCreatedBy() != null ? label.getCreatedBy().getId() : null,
                label.getCreatedAt()
        );
    }
}
