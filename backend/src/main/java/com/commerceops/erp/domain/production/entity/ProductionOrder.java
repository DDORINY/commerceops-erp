package com.commerceops.erp.domain.production.entity;

import com.commerceops.erp.domain.production.enums.ProductionOrderStatus;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "production_orders",
        indexes = {
                @Index(name = "idx_production_orders_status", columnList = "status"),
                @Index(name = "idx_production_orders_warehouse_id", columnList = "warehouse_id"),
                @Index(name = "idx_production_orders_created_at", columnList = "created_at")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "production_number", nullable = false, unique = true, length = 40)
    private String productionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductionOrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "planned_quantity", nullable = false)
    private Integer plannedQuantity;

    @Column(name = "completed_quantity", nullable = false)
    @Builder.Default
    private Integer completedQuantity = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductionOrderItem> items = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void replaceItems(List<ProductionOrderItem> newItems) {
        items.clear();
        newItems.forEach(this::addItem);
        recalculatePlannedQuantity();
    }

    public void addItem(ProductionOrderItem item) {
        items.add(item);
        item.assignOrder(this);
    }

    public void update(Warehouse warehouse, String memo, User updatedBy, List<ProductionOrderItem> newItems) {
        this.warehouse = warehouse;
        this.memo = memo;
        this.updatedBy = updatedBy;
        replaceItems(newItems);
    }

    public void start(User updatedBy) {
        this.status = ProductionOrderStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    public void complete(int completedQuantity, String memo, User updatedBy) {
        this.status = ProductionOrderStatus.COMPLETED;
        this.completedQuantity = completedQuantity;
        this.completedAt = LocalDateTime.now();
        if (memo != null) {
            this.memo = memo;
        }
        this.updatedBy = updatedBy;
    }

    public void cancel(String memo, User updatedBy) {
        this.status = ProductionOrderStatus.CANCELLED;
        this.memo = memo;
        this.updatedBy = updatedBy;
    }

    public void recalculatePlannedQuantity() {
        this.plannedQuantity = items.stream()
                .mapToInt(ProductionOrderItem::getPlannedQuantity)
                .sum();
    }
}
