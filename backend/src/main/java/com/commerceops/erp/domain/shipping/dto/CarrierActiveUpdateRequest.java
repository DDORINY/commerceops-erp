package com.commerceops.erp.domain.shipping.dto;

import jakarta.validation.constraints.NotNull;

public record CarrierActiveUpdateRequest(
        @NotNull Boolean active
) {
}
