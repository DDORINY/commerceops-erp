package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.entity.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public record AdminProductListResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        Integer price,
        String productCode,
        String brand,
        Integer originalPrice,
        Integer discountPrice,
        Integer purchasePrice,
        BigDecimal marginRate,
        String tags,
        Integer stockQuantity,
        String imageUrl,
        String status,
        List<ProductOptionGroup> options,
        LocalDateTime createdAt
) {
    public static AdminProductListResponse from(Product product) {
        return new AdminProductListResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getPrice(),
                product.getProductCode(),
                product.getBrand(),
                product.getOriginalPrice(),
                product.getDiscountPrice(),
                product.getPurchasePrice(),
                calculateMarginRate(product.getPrice(), product.getPurchasePrice()),
                product.getTags(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getStatus().name(),
                product.getOptions() != null ? product.getOptions() : List.of(),
                product.getCreatedAt()
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
