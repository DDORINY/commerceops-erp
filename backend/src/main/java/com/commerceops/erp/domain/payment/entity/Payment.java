package com.commerceops.erp.domain.payment.entity;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.payment.enums.PaymentMethod;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column
    private Integer paidAmount;

    @Column(length = 100)
    private String transactionId;

    @Column(length = 120, unique = true)
    private String idempotencyKey;

    @Column(length = 30)
    private String provider;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void complete(PaymentMethod paymentMethod, Integer paidAmount, String transactionId, String idempotencyKey) {
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.PAID;
        this.paidAmount = paidAmount;
        this.transactionId = transactionId;
        this.idempotencyKey = idempotencyKey;
        this.provider = "MOCK_PROVIDER";
    }

    public void cancelReadyPayment() {
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.paidAmount = 0;
    }

    public void refund() {
        this.paymentStatus = PaymentStatus.REFUNDED;
    }
}
