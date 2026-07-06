package com.commerceops.erp.domain.shipment.dto;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.shipment.entity.Shipment;

import java.time.LocalDateTime;

public record ShipmentResponse(
        Long shipmentId,
        Long orderId,
        String orderNumber,
        String receiverName,
        String receiverPhone,
        String address,
        String status,
        String trackingNumber,
        String carrier,
        String trackingNumberSource,
        LocalDateTime trackingNumberIssuedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt
) {
    public static ShipmentResponse from(Shipment shipment) {
        Order order = shipment.getOrder();
        String fullAddress = order.getDetailAddress() != null
                ? order.getAddress() + " " + order.getDetailAddress()
                : order.getAddress();

        return new ShipmentResponse(
                shipment.getId(),
                order.getId(),
                order.getOrderNumber(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                fullAddress,
                shipment.getStatus().name(),
                shipment.getTrackingNumber(),
                shipment.getCarrier(),
                shipment.getTrackingNumberSource() != null ? shipment.getTrackingNumberSource().name() : null,
                shipment.getTrackingNumberIssuedAt(),
                shipment.getShippedAt(),
                shipment.getDeliveredAt(),
                shipment.getCreatedAt()
        );
    }
}
