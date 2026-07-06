package com.commerceops.erp.domain.production.entity;

import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.sku.entity.Sku;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(
        name = "production_order_items",
        indexes = {
                @Index(name = "idx_production_order_items_order_id", columnList = "production_order_id"),
                @Index(name = "idx_production_order_items_sku_id", columnList = "sku_id"),
                @Index(name = "idx_production_order_items_product_id", columnList = "product_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductionOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "planned_quantity", nullable = false)
    private Integer plannedQuantity;

    @Column(name = "completed_quantity", nullable = false)
    @Builder.Default
    private Integer completedQuantity = 0;

    void assignOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public void complete(int quantity) {
        this.completedQuantity = quantity;
    }
}
