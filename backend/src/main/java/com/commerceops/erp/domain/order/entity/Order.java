package com.commerceops.erp.domain.order.entity;

import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column
    private Integer discountAmount;

    @Column(length = 50)
    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false)
    private String address;

    @Column
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void updateOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void markAsPaid() {
        this.status = OrderStatus.PAID;
        this.paymentStatus = PaymentStatus.DONE;
    }

    public void markPaymentFailed() {
        this.status = OrderStatus.PAYMENT_FAILED;
        this.paymentStatus = PaymentStatus.ABORTED;
    }

    public void retryPayment() {
        this.status = OrderStatus.PENDING_PAYMENT;
        this.paymentStatus = PaymentStatus.READY;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public void cancel(boolean paid) {
        this.status = OrderStatus.CANCELLED;
        this.paymentStatus = paid ? PaymentStatus.REFUNDED : PaymentStatus.CANCELLED;
    }
}
