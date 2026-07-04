package com.commerceops.erp.domain.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WarehouseStockAllocateRequest(
        @NotNull Long warehouseId,
        @NotNull Long productId,
        @Min(1) int quantity
) {}
