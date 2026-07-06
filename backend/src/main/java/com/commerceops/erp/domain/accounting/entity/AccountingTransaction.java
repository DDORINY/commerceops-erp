package com.commerceops.erp.domain.accounting.entity;

import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionDirection;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import com.commerceops.erp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounting_transactions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class AccountingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_id")
    private AccountingLedger ledger;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountingTransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountingTransactionDirection direction;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountingReferenceType referenceType;

    @Column(nullable = false)
    private Long referenceId;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 500)
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
