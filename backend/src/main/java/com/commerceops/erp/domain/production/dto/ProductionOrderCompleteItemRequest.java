package com.commerceops.erp.domain.production.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductionOrderCompleteItemRequest(
        @NotNull Long skuId,
        @NotNull @Min(0) Integer completedQuantity
) {
}
