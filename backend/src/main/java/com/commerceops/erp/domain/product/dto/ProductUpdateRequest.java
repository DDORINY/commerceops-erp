package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.List;

public record ProductUpdateRequest(
        Long categoryId,
        String name,
        String description,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        String productCode,
        String brand,
        String manufacturer,
        String modelName,
        String origin,

        @Min(value = 0, message = "originalPrice must be greater than or equal to 0")
        Integer originalPrice,

        @Min(value = 0, message = "discountPrice must be greater than or equal to 0")
        Integer discountPrice,

        @Min(value = 0, message = "purchasePrice must be greater than or equal to 0")
        Integer purchasePrice,

        String searchKeywords,
        String tags,
        LocalDateTime saleStartAt,
        LocalDateTime saleEndAt,
        String deliveryInfo,
        String seoTitle,
        String seoDescription,
        String seoKeywords,

        @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity,

        String imageUrl,
        ProductStatus status,
        List<ProductOptionGroup> options
) {}
