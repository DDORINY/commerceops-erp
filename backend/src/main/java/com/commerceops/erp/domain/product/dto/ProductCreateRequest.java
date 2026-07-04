package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductCreateRequest(
        @NotNull(message = "카테고리는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "상품명은 필수입니다.")
        String name,

        String description,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @NotNull(message = "재고 수량은 필수입니다.")
        @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity,

        String imageUrl,

        @NotNull(message = "상품 상태는 필수입니다.")
        ProductStatus status,

        List<ProductOptionGroup> options
) {}
