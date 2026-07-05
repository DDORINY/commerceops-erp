package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.enums.ProductDisplayStatus;
import com.commerceops.erp.domain.product.enums.ProductSalesStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductBulkStatusUpdateRequest(
        @NotEmpty List<Long> productIds,
        ProductSalesStatus salesStatus,
        ProductDisplayStatus displayStatus,
        @Size(max = 500) String reason
) {
}
