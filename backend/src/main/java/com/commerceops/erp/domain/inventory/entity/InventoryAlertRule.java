package com.commerceops.erp.domain.inventory.entity;

import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
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
import jakarta.persistence.UniqueConstraint;
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
        name = "inventory_alert_rules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventory_alert_rules_sku_warehouse",
                columnNames = {"sku_id", "warehouse_id"}
        ),
        indexes = {
                @Index(name = "idx_inventory_alert_rules_sku", columnList = "sku_id"),
                @Index(name = "idx_inventory_alert_rules_warehouse", columnList = "warehouse_id"),
                @Index(name = "idx_inventory_alert_rules_active", columnList = "active")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class InventoryAlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(name = "threshold_quantity", nullable = false)
    private int thresholdQuantity;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(length = 500)
    private String memo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(Warehouse warehouse, int thresholdQuantity, String memo) {
        this.warehouse = warehouse;
        this.thresholdQuantity = thresholdQuantity;
        this.memo = memo;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }
}
