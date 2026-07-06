package com.commerceops.erp.domain.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record StockCountCreateRequest(
        @NotNull Long warehouseId,
        String memo
) {
}
