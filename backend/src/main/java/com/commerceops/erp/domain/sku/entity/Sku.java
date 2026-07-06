package com.commerceops.erp.domain.sku.entity;

import com.commerceops.erp.domain.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "skus",
        indexes = {
                @Index(name = "idx_skus_product_id", columnList = "product_id"),
                @Index(name = "idx_skus_active", columnList = "active"),
                @Index(name = "idx_skus_barcode", columnList = "barcode")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "option_signature", length = 500)
    private String optionSignature;

    @Column(name = "sku_code", nullable = false, length = 100, unique = true)
    private String skuCode;

    @Column(length = 100, unique = true)
    private String barcode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "safety_stock_quantity", nullable = false)
    private Integer safetyStockQuantity;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(String optionSignature, String skuCode, String barcode, String name, Integer safetyStockQuantity) {
        this.optionSignature = optionSignature;
        this.skuCode = skuCode;
        this.barcode = barcode;
        this.name = name;
        this.safetyStockQuantity = safetyStockQuantity;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }

    public void changeBarcode(String barcode) {
        this.barcode = barcode;
    }
}
