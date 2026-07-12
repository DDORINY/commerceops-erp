package com.commerceops.erp.domain.accounting.entity;

import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.SettlementBatchItemStatus;
import com.commerceops.erp.domain.accounting.enums.SettlementBatchItemType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "settlement_batch_items",
        indexes = {
                @Index(name = "idx_settlement_batch_items_batch_type", columnList = "settlement_batch_id, item_type"),
                @Index(name = "idx_settlement_batch_items_reference", columnList = "reference_type, reference_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SettlementBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_batch_id", nullable = false)
    private SettlementBatch settlementBatch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountingReferenceType referenceType;

    @Column(nullable = false)
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private SettlementBatchItemType itemType;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 500)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementBatchItemStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    void assignBatch(SettlementBatch settlementBatch) {
        this.settlementBatch = settlementBatch;
    }
}
