package com.commerceops.erp.domain.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockCountItemInput(
        @NotNull Long skuId,
        @Min(0) Integer countedQuantity,
        String memo
) {
}
