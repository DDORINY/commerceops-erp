package com.commerceops.erp.domain.shipment.dto;

public record ShipmentLabelPreviewResponse(
        Long labelId,
        String labelFormat,
        String trackingNumber,
        String carrier,
        String orderNumber,
        String receiverName,
        String receiverPhone,
        String address,
        Integer printCount,
        String html
) {
}
