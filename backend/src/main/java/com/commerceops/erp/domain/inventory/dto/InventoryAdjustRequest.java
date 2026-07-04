package com.commerceops.erp.domain.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryAdjustRequest(
        @NotNull Long warehouseId,
        @NotNull Long productId,
        @NotNull @Min(0) Integer quantity,
        String memo
) {}
