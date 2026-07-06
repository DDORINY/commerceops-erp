package com.commerceops.erp.domain.outbound.entity;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.outbound.enums.OutboundOrderStatus;
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
        name = "outbound_orders",
        indexes = {
                @Index(name = "idx_outbound_orders_order_id", columnList = "order_id"),
                @Index(name = "idx_outbound_orders_warehouse_id", columnList = "warehouse_id"),
                @Index(name = "idx_outbound_orders_status", columnList = "status"),
                @Index(name = "idx_outbound_orders_requested_at", columnList = "requested_at")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class OutboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outbound_number", nullable = false, unique = true, length = 50)
    private String outboundNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboundOrderStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "picked_at")
    private LocalDateTime pickedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(length = 500)
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Builder.Default
    @OneToMany(mappedBy = "outboundOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundOrderItem> items = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void replaceItems(List<OutboundOrderItem> nextItems) {
        items.clear();
        nextItems.forEach(item -> {
            item.assignOutboundOrder(this);
            items.add(item);
        });
    }

    public void update(Warehouse warehouse, String memo, User actor) {
        this.warehouse = warehouse;
        this.memo = memo;
        this.updatedBy = actor;
    }

    public void markPicked(User actor) {
        this.status = OutboundOrderStatus.PICKED;
        this.pickedAt = LocalDateTime.now();
        this.updatedBy = actor;
        this.items.forEach(OutboundOrderItem::markFullyPicked);
    }

    public void cancel(User actor) {
        this.status = OutboundOrderStatus.CANCELLED;
        this.updatedBy = actor;
    }
}
