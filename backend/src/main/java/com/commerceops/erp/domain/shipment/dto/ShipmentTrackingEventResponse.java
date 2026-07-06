package com.commerceops.erp.domain.shipment.dto;

import com.commerceops.erp.domain.shipment.entity.ShipmentTrackingEvent;

import java.time.LocalDateTime;

public record ShipmentTrackingEventResponse(
        Long id,
        Long shipmentId,
        String status,
        String description,
        LocalDateTime eventAt,
        String rawPayload,
        LocalDateTime createdAt
) {
    public static ShipmentTrackingEventResponse from(ShipmentTrackingEvent event) {
        return new ShipmentTrackingEventResponse(
                event.getId(),
                event.getShipment().getId(),
                event.getStatus().name(),
                event.getDescription(),
                event.getEventAt(),
                event.getRawPayload(),
                event.getCreatedAt()
        );
    }
}
