package com.commerceops.erp.domain.shipment.entity;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.enums.TrackingNumberSource;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(length = 100)
    private String trackingNumber;

    @Column(length = 100)
    private String carrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_number_source", length = 20)
    private TrackingNumberSource trackingNumberSource;

    @Column(name = "tracking_number_issued_at")
    private LocalDateTime trackingNumberIssuedAt;

    @Column
    private LocalDateTime shippedAt;

    @Column
    private LocalDateTime deliveredAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void updateTracking(String trackingNumber, String carrier) {
        updateTracking(trackingNumber, carrier, TrackingNumberSource.MANUAL);
    }

    public void updateTracking(String trackingNumber, String carrier, TrackingNumberSource source) {
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.trackingNumberSource = source;
        this.trackingNumberIssuedAt = LocalDateTime.now();
        markInTransit();
    }

    public void markInTransit() {
        this.status = ShipmentStatus.IN_TRANSIT;
        if (this.shippedAt == null) {
            this.shippedAt = LocalDateTime.now();
        }
    }

    public void changeStatus(ShipmentStatus status) {
        this.status = status;
        if (status == ShipmentStatus.IN_TRANSIT && this.shippedAt == null) {
            this.shippedAt = LocalDateTime.now();
        }
        if (status == ShipmentStatus.DELIVERED && this.deliveredAt == null) {
            this.deliveredAt = LocalDateTime.now();
        }
    }

    public void markDelivered() {
        changeStatus(ShipmentStatus.DELIVERED);
    }

    public void cancel() {
        this.status = ShipmentStatus.CANCELLED;
    }
}
