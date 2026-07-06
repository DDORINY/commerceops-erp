package com.commerceops.erp.domain.sku.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkuCreateRequest(
        @NotNull Long productId,
        @Size(max = 500) String optionSignature,
        @Size(max = 100) String skuCode,
        @Size(max = 100) String barcode,
        @Size(max = 200) String name,
        @Min(0) Integer safetyStockQuantity,
        Boolean active
) {
}
