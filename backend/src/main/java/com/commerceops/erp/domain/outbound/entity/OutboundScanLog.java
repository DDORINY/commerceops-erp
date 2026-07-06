package com.commerceops.erp.domain.outbound.entity;

import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "outbound_scan_logs",
        indexes = {
                @Index(name = "idx_outbound_scan_logs_outbound_order_id", columnList = "outbound_order_id"),
                @Index(name = "idx_outbound_scan_logs_item_id", columnList = "outbound_order_item_id"),
                @Index(name = "idx_outbound_scan_logs_barcode", columnList = "barcode"),
                @Index(name = "idx_outbound_scan_logs_created_at", columnList = "created_at")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class OutboundScanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_order_id", nullable = false)
    private OutboundOrder outboundOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_order_item_id", nullable = false)
    private OutboundOrderItem outboundOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id")
    private Sku sku;

    @Column(nullable = false, length = 100)
    private String barcode;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scanned_by")
    private User scannedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
