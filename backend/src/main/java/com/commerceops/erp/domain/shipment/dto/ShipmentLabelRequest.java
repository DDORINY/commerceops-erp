package com.commerceops.erp.domain.shipment.dto;

import jakarta.validation.constraints.Size;

public record ShipmentLabelRequest(
        @Size(max = 50) String labelFormat
) {
}
