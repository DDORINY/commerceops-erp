package com.commerceops.erp.domain.sku.dto;

import jakarta.validation.constraints.NotNull;

public record SkuActiveUpdateRequest(
        @NotNull Boolean active
) {
}
