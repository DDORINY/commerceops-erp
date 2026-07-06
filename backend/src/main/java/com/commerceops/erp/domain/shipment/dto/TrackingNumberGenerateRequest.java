package com.commerceops.erp.domain.shipment.dto;

import jakarta.validation.constraints.NotBlank;

public record TrackingNumberGenerateRequest(
        @NotBlank String carrier
) {
}
