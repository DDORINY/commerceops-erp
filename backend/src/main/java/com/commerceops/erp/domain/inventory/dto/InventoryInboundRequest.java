package com.commerceops.erp.domain.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryInboundRequest(
        @NotNull Long warehouseId,
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity,
        String memo
) {}
