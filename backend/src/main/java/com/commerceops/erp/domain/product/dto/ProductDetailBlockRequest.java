package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.enums.ProductDetailBlockType;

public record ProductDetailBlockRequest(
        ProductDetailBlockType blockType,
        String title,
        String content,
        String imageUrl,
        String specJson,
        Integer sortOrder,
        Boolean visible
) {
}
