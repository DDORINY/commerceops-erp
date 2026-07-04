package com.commerceops.erp.domain.warehouse.entity;

import com.commerceops.erp.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "warehouse_stocks",
        uniqueConstraints = @UniqueConstraint(name = "uk_warehouse_stock", columnNames = {"warehouse_id", "product_id"})
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WarehouseStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    @Builder.Default
    private int reservedQuantity = 0;

    @Version
    private long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void increase(int amount) {
        this.quantity += amount;
    }

    public void decrease(int amount) {
        this.quantity -= amount;
    }

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public void reserve(int amount) {
        this.reservedQuantity += amount;
    }

    public void shipReserved(int amount) {
        this.reservedQuantity -= amount;
        this.quantity -= amount;
    }

    public void releaseReserved(int amount) {
        this.reservedQuantity -= amount;
    }

    public void adjustQuantity(int targetQuantity) {
        this.quantity = targetQuantity;
    }
}
