package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.enums.ProductDisplayStatus;
import com.commerceops.erp.domain.product.enums.ProductSalesStatus;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ProductCreateRequest(
        @NotNull(message = "移댄뀒怨좊━???꾩닔?낅땲??")
        Long categoryId,

        @NotBlank(message = "?곹뭹紐낆? ?꾩닔?낅땲??")
        String name,

        String description,

        @NotNull(message = "媛寃⑹? ?꾩닔?낅땲??")
        @Min(value = 0, message = "媛寃⑹? 0???댁긽?댁뼱???⑸땲??")
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

        ProductSalesStatus salesStatus,
        ProductDisplayStatus displayStatus,

        @Min(value = 0, message = "safetyStockQuantity must be greater than or equal to 0")
        Integer safetyStockQuantity,

        @NotNull(message = "?ш퀬 ?섎웾? ?꾩닔?낅땲??")
        @Min(value = 0, message = "?ш퀬 ?섎웾? 0 ?댁긽?댁뼱???⑸땲??")
        Integer stockQuantity,

        String imageUrl,

        @NotNull(message = "?곹뭹 ?곹깭???꾩닔?낅땲??")
        ProductStatus status,

        List<ProductOptionGroup> options
) {}


