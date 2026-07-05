package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.enums.ProductDisplayStatus;
import com.commerceops.erp.domain.product.enums.ProductSalesStatus;
import jakarta.validation.constraints.Min;

public record ProductStatusUpdateRequest(
        ProductSalesStatus salesStatus,
        ProductDisplayStatus displayStatus,
        @Min(value = 0, message = "safetyStockQuantity must be greater than or equal to 0")
        Integer safetyStockQuantity
) {}
