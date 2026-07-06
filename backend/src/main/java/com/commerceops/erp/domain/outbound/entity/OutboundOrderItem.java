package com.commerceops.erp.domain.outbound.entity;

import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.sku.entity.Sku;
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
        name = "outbound_order_items",
        indexes = {
                @Index(name = "idx_outbound_order_items_outbound_order_id", columnList = "outbound_order_id"),
                @Index(name = "idx_outbound_order_items_order_item_id", columnList = "order_item_id"),
                @Index(name = "idx_outbound_order_items_sku_id", columnList = "sku_id"),
                @Index(name = "idx_outbound_order_items_product_id", columnList = "product_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class OutboundOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_order_id", nullable = false)
    private OutboundOrder outboundOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id")
    private Sku sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "picked_quantity", nullable = false)
    private Integer pickedQuantity;

    @Column(name = "scanned_quantity", nullable = false)
    private Integer scannedQuantity;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    void assignOutboundOrder(OutboundOrder outboundOrder) {
        this.outboundOrder = outboundOrder;
    }

    void markFullyPicked() {
        this.pickedQuantity = this.quantity;
    }
}
