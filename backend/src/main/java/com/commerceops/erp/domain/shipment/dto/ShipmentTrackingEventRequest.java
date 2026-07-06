package com.commerceops.erp.domain.shipment.dto;

import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ShipmentTrackingEventRequest(
        @NotNull ShipmentStatus status,
        @NotBlank @Size(max = 500) String description,
        LocalDateTime eventAt,
        @Size(max = 5000) String rawPayload
) {
}
