package com.commerceops.erp.domain.barcode.entity;

import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.user.entity.User;
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
        name = "barcode_labels",
        indexes = {
                @Index(name = "idx_barcode_labels_sku_id", columnList = "sku_id"),
                @Index(name = "idx_barcode_labels_barcode", columnList = "barcode"),
                @Index(name = "idx_barcode_labels_created_at", columnList = "created_at")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class BarcodeLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @Column(nullable = false, length = 100)
    private String barcode;

    @Column(name = "label_format", nullable = false, length = 50)
    private String labelFormat;

    @Column(name = "print_count", nullable = false)
    @Builder.Default
    private Integer printCount = 0;

    @Column(name = "last_printed_at")
    private LocalDateTime lastPrintedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void markPrinted() {
        this.printCount = this.printCount + 1;
        this.lastPrintedAt = LocalDateTime.now();
    }
}
