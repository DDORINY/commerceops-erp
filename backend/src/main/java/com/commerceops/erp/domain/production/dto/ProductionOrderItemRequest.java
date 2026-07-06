package com.commerceops.erp.domain.production.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductionOrderItemRequest(
        @NotNull Long skuId,
        @NotNull @Min(1) Integer plannedQuantity
) {
}
