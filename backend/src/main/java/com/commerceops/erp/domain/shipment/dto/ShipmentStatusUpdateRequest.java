package com.commerceops.erp.domain.shipment.dto;

import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShipmentStatusUpdateRequest(
        @NotNull ShipmentStatus status,
        @Size(max = 500) String description
) {
}
