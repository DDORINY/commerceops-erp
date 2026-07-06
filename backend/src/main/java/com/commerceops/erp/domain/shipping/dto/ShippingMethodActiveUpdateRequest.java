package com.commerceops.erp.domain.shipping.dto;

import jakarta.validation.constraints.NotNull;

public record ShippingMethodActiveUpdateRequest(
        @NotNull Boolean active
) {
}
