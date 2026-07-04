package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.entity.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public record AdminProductResponse(
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
        Integer purchasePrice,
        BigDecimal marginRate,
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
        List<ProductOptionGroup> options,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminProductResponse from(Product product) {
        return new AdminProductResponse(
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
                product.getPurchasePrice(),
                calculateMarginRate(product.getPrice(), product.getPurchasePrice()),
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
                product.getOptions() != null ? product.getOptions() : List.of(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private static BigDecimal calculateMarginRate(Integer price, Integer purchasePrice) {
        if (price == null || purchasePrice == null || price <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(price - purchasePrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(price), 2, RoundingMode.HALF_UP);
    }
}
