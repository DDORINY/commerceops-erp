package com.commerceops.erp.domain.shipment.dto;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.shipment.entity.ShipmentLabel;

import java.time.LocalDateTime;

public record ShipmentLabelResponse(
        Long id,
        Long shipmentId,
        Long orderId,
        String orderNumber,
        String receiverName,
        String trackingNumber,
        String carrier,
        String labelFormat,
        Integer printCount,
        LocalDateTime lastPrintedAt,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static ShipmentLabelResponse from(ShipmentLabel label) {
        Order order = label.getShipment().getOrder();
        return new ShipmentLabelResponse(
                label.getId(),
                label.getShipment().getId(),
                order.getId(),
                order.getOrderNumber(),
                order.getReceiverName(),
                label.getTrackingNumber(),
                label.getCarrier(),
                label.getLabelFormat(),
                label.getPrintCount(),
                label.getLastPrintedAt(),
                label.getCreatedBy() != null ? label.getCreatedBy().getId() : null,
                label.getCreatedAt()
        );
    }
}
