package com.commerceops.erp.domain.shipment.dto;

import jakarta.validation.constraints.NotBlank;

public record TrackingUpdateRequest(
        @NotBlank String trackingNumber,
        @NotBlank String carrier
) {}
