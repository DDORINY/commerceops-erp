package com.commerceops.erp.domain.warehouse.entity;

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
        name = "warehouse_locations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_warehouse_locations_warehouse_code",
                columnNames = {"warehouse_id", "code"}
        ),
        indexes = {
                @Index(name = "idx_warehouse_locations_warehouse", columnList = "warehouse_id"),
                @Index(name = "idx_warehouse_locations_active", columnList = "active"),
                @Index(name = "idx_warehouse_locations_code", columnList = "code")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WarehouseLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 60)
    private String zone;

    @Column(length = 60)
    private String aisle;

    @Column(length = 60)
    private String rack;

    @Column(length = 60)
    private String cell;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(String code, String name, String zone, String aisle, String rack, String cell) {
        this.code = code;
        this.name = name;
        this.zone = zone;
        this.aisle = aisle;
        this.rack = rack;
        this.cell = cell;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }
}
