package com.commerceops.erp.domain.barcode.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BarcodeStockChangeRequest(
        @NotNull Long warehouseId,
        @NotNull @Min(1) Integer quantity,
        String memo
) {
}
