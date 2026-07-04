package com.commerceops.erp.domain.product.entity;

import com.commerceops.erp.domain.category.entity.Category;
import com.commerceops.erp.domain.product.converter.OptionGroupListConverter;
import com.commerceops.erp.domain.product.dto.ProductOptionGroup;
import com.commerceops.erp.domain.product.enums.ProductStatus;
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

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

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
                       ProductStatus status, List<ProductOptionGroup> options) {
        if (category != null) this.category = category;
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (stockQuantity != null) this.stockQuantity = stockQuantity;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (status != null) this.status = status;
        if (options != null) this.options = options;
    }

    public void softDelete() {
        this.status = ProductStatus.DELETED;
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
