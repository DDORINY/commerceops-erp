package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.Min;

import java.util.List;

public record ProductUpdateRequest(
        Long categoryId,
        String name,
        String description,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity,

        String imageUrl,
        ProductStatus status,
        List<ProductOptionGroup> options
) {}
