package com.commerceops.erp.domain.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryAlertRuleRequest(
        @NotNull Long skuId,
        Long warehouseId,
        @NotNull @Min(0) Integer thresholdQuantity,
        @Size(max = 500) String memo
) {
}
