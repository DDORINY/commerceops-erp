package com.commerceops.erp.domain.product.dto;

import java.util.List;

public record ProductBulkStatusUpdateResponse(
        int updatedCount,
        List<AdminProductListResponse> products
) {
}
