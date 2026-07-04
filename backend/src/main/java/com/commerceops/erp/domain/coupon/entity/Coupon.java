package com.commerceops.erp.domain.coupon.entity;

import com.commerceops.erp.domain.coupon.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DiscountType discountType;

    @Column(nullable = false)
    private Integer discountValue;

    @Column(nullable = false)
    private Integer minOrderAmount;

    @Column(nullable = false)
    private Integer maxUsage;

    @Column(nullable = false)
    private Integer usedCount;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public int calculateDiscount(int orderAmount) {
        if (discountType == DiscountType.FIXED) {
            return Math.min(discountValue, orderAmount);
        }
        return (int) Math.round(orderAmount * discountValue / 100.0);
    }

    public boolean isValid(int orderAmount) {
        return active
                && usedCount < maxUsage
                && LocalDateTime.now().isBefore(expiresAt)
                && orderAmount >= minOrderAmount;
    }

    public void use() {
        this.usedCount++;
    }

    public void deactivate() {
        this.active = false;
    }
}
