package com.commerceops.erp.domain.production.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductionOrderUpdateRequest(
        @NotNull Long warehouseId,
        @Size(max = 1000) String memo,
        @NotEmpty List<@Valid ProductionOrderItemRequest> items
) {
}
