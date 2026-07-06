package com.commerceops.erp.domain.warehouse.dto;

import jakarta.validation.constraints.NotNull;

public record WarehouseLocationActiveRequest(
        @NotNull Boolean active
) {
}
