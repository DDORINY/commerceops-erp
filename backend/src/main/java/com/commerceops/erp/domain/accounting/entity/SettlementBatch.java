package com.commerceops.erp.domain.accounting.entity;

import com.commerceops.erp.domain.accounting.enums.SettlementBatchStatus;
import com.commerceops.erp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "settlement_batches",
        indexes = {
                @Index(name = "idx_settlement_batches_period", columnList = "period_start, period_end"),
                @Index(name = "idx_settlement_batches_status", columnList = "status")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SettlementBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String batchNumber;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementBatchStatus status;

    @Column(nullable = false)
    private Long totalSales;

    @Column(nullable = false)
    private Long totalRefunds;

    @Column(nullable = false)
    private Long totalShippingFee;

    @Column(nullable = false)
    private Long totalShippingCost;

    @Column
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    private User closedBy;

    @OneToMany(mappedBy = "settlementBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SettlementBatchItem> items = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void addItem(SettlementBatchItem item) {
        this.items.add(item);
        item.assignBatch(this);
    }

    public void close(User actor) {
        this.status = SettlementBatchStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.closedBy = actor;
    }
}
