package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.entity.Product;

import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        Integer price,
        String productCode,
        String brand,
        String manufacturer,
        String modelName,
        String origin,
        Integer originalPrice,
        Integer discountPrice,
        String searchKeywords,
        String tags,
        LocalDateTime saleStartAt,
        LocalDateTime saleEndAt,
        String deliveryInfo,
        String seoTitle,
        String seoDescription,
        String seoKeywords,
        Integer stockQuantity,
        String imageUrl,
        String status,
        String salesStatus,
        Boolean purchasable,
        String stockDisplayStatus,
        String stockDisplayText,
        Integer remainingStockQuantity,
        List<ProductOptionGroup> options,
        List<ProductDetailBlockResponse> detailBlocks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        return from(product, List.of());
    }

    public static ProductResponse from(Product product, List<ProductDetailBlockResponse> detailBlocks) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getProductCode(),
                product.getBrand(),
                product.getManufacturer(),
                product.getModelName(),
                product.getOrigin(),
                product.getOriginalPrice(),
                product.getDiscountPrice(),
                product.getSearchKeywords(),
                product.getTags(),
                product.getSaleStartAt(),
                product.getSaleEndAt(),
                product.getDeliveryInfo(),
                product.getSeoTitle(),
                product.getSeoDescription(),
                product.getSeoKeywords(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getStatus().name(),
                product.getSalesStatus().name(),
                product.isPurchasable(LocalDateTime.now()),
                product.getStockDisplayStatus().name(),
                product.getStockDisplayText(),
                product.getStockQuantity(),
                product.getOptions() != null ? product.getOptions() : List.of(),
                detailBlocks != null ? detailBlocks : List.of(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
