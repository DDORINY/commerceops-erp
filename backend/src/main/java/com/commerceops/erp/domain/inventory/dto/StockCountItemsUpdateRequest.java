package com.commerceops.erp.domain.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StockCountItemsUpdateRequest(
        @NotEmpty List<@Valid StockCountItemInput> items
) {
}
