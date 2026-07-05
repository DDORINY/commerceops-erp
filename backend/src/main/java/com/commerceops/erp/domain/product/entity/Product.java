package com.commerceops.erp.domain.product.entity;

import com.commerceops.erp.domain.category.entity.Category;
import com.commerceops.erp.domain.product.converter.OptionGroupListConverter;
import com.commerceops.erp.domain.product.dto.ProductOptionGroup;
import com.commerceops.erp.domain.product.enums.ProductDisplayStatus;
import com.commerceops.erp.domain.product.enums.ProductSalesStatus;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.enums.StockDisplayStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(length = 80)
    private String productCode;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String modelName;

    @Column(length = 100)
    private String origin;

    @Column
    private Integer originalPrice;

    @Column
    private Integer discountPrice;

    @Column
    private Integer purchasePrice;

    @Column(columnDefinition = "TEXT")
    private String searchKeywords;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column
    private LocalDateTime saleStartAt;

    @Column
    private LocalDateTime saleEndAt;

    @Column(columnDefinition = "TEXT")
    private String deliveryInfo;

    @Column(length = 200)
    private String seoTitle;

    @Column(length = 500)
    private String seoDescription;

    @Column(columnDefinition = "TEXT")
    private String seoKeywords;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ProductSalesStatus salesStatus = ProductSalesStatus.ON_SALE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ProductDisplayStatus displayStatus = ProductDisplayStatus.VISIBLE;

    @Column
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer safetyStockQuantity = 5;

    @Convert(converter = OptionGroupListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<ProductOptionGroup> options;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(Category category, String name, String description,
                       Integer price, Integer stockQuantity, String imageUrl,
                       ProductStatus status, List<ProductOptionGroup> options,
                       String productCode, String brand, String manufacturer,
                       String modelName, String origin, Integer originalPrice,
                       Integer discountPrice, Integer purchasePrice, String searchKeywords,
                       String tags, LocalDateTime saleStartAt, LocalDateTime saleEndAt,
                       String deliveryInfo, String seoTitle, String seoDescription,
                       String seoKeywords, ProductSalesStatus salesStatus,
                       ProductDisplayStatus displayStatus, Integer safetyStockQuantity) {
        if (category != null) this.category = category;
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (stockQuantity != null) this.stockQuantity = stockQuantity;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (status != null) this.status = status;
        if (options != null) this.options = options;
        if (productCode != null) this.productCode = productCode;
        if (brand != null) this.brand = brand;
        if (manufacturer != null) this.manufacturer = manufacturer;
        if (modelName != null) this.modelName = modelName;
        if (origin != null) this.origin = origin;
        if (originalPrice != null) this.originalPrice = originalPrice;
        if (discountPrice != null) this.discountPrice = discountPrice;
        if (purchasePrice != null) this.purchasePrice = purchasePrice;
        if (searchKeywords != null) this.searchKeywords = searchKeywords;
        if (tags != null) this.tags = tags;
        if (saleStartAt != null) this.saleStartAt = saleStartAt;
        if (saleEndAt != null) this.saleEndAt = saleEndAt;
        if (deliveryInfo != null) this.deliveryInfo = deliveryInfo;
        if (seoTitle != null) this.seoTitle = seoTitle;
        if (seoDescription != null) this.seoDescription = seoDescription;
        if (seoKeywords != null) this.seoKeywords = seoKeywords;
        if (salesStatus != null) this.salesStatus = salesStatus;
        if (displayStatus != null) this.displayStatus = displayStatus;
        if (safetyStockQuantity != null) this.safetyStockQuantity = safetyStockQuantity;
        syncLegacyStatus();
    }

    public void softDelete() {
        this.status = ProductStatus.DELETED;
        this.salesStatus = ProductSalesStatus.DISCONTINUED;
        this.displayStatus = ProductDisplayStatus.HIDDEN;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateOperationStatus(ProductSalesStatus salesStatus,
                                      ProductDisplayStatus displayStatus,
                                      Integer safetyStockQuantity) {
        if (salesStatus != null) this.salesStatus = salesStatus;
        if (displayStatus != null) this.displayStatus = displayStatus;
        if (safetyStockQuantity != null) this.safetyStockQuantity = safetyStockQuantity;
        syncLegacyStatus();
    }

    public boolean isPubliclyVisible() {
        return deletedAt == null
                && displayStatus == ProductDisplayStatus.VISIBLE
                && salesStatus != ProductSalesStatus.DRAFT
                && salesStatus != ProductSalesStatus.DISCONTINUED
                && status != ProductStatus.DELETED;
    }

    public boolean isPurchasable(LocalDateTime now) {
        return isPubliclyVisible()
                && salesStatus == ProductSalesStatus.ON_SALE
                && stockQuantity != null
                && stockQuantity > 0
                && (saleStartAt == null || !now.isBefore(saleStartAt))
                && (saleEndAt == null || !now.isAfter(saleEndAt));
    }

    public StockDisplayStatus getStockDisplayStatus() {
        if (stockQuantity == null || stockQuantity <= 0 || salesStatus == ProductSalesStatus.SOLD_OUT) {
            return StockDisplayStatus.SOLD_OUT;
        }
        int threshold = safetyStockQuantity != null ? safetyStockQuantity : 0;
        if (threshold > 0 && stockQuantity <= threshold) {
            return StockDisplayStatus.LOW_STOCK;
        }
        return StockDisplayStatus.IN_STOCK;
    }

    public String getStockDisplayText() {
        return switch (getStockDisplayStatus()) {
            case SOLD_OUT -> "품절";
            case LOW_STOCK -> "품절 임박";
            case IN_STOCK -> "구매 가능";
        };
    }

    private void syncLegacyStatus() {
        if (deletedAt != null) {
            this.status = ProductStatus.DELETED;
        } else if (displayStatus == ProductDisplayStatus.HIDDEN
                || salesStatus == ProductSalesStatus.DRAFT
                || salesStatus == ProductSalesStatus.DISCONTINUED
                || salesStatus == ProductSalesStatus.PAUSED) {
            this.status = ProductStatus.HIDDEN;
        } else if (salesStatus == ProductSalesStatus.SOLD_OUT || (stockQuantity != null && stockQuantity <= 0)) {
            this.status = ProductStatus.SOLD_OUT;
        } else {
            this.status = ProductStatus.ON_SALE;
        }
    }

    public void decrementStock(int amount) {
        this.stockQuantity -= amount;
    }

    public void incrementStock(int amount) {
        this.stockQuantity += amount;
    }

    public void adjustStock(int quantity) {
        this.stockQuantity = quantity;
    }
}
