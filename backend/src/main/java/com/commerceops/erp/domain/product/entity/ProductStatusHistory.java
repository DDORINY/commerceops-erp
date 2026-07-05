package com.commerceops.erp.domain.product.entity;

import com.commerceops.erp.domain.product.enums.ProductDisplayStatus;
import com.commerceops.erp.domain.product.enums.ProductSalesStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_status_histories")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ProductStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Long changedByUserId;

    @Column(length = 100)
    private String changedByEmail;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ProductSalesStatus previousSalesStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ProductSalesStatus newSalesStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ProductDisplayStatus previousDisplayStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ProductDisplayStatus newDisplayStatus;

    @Column(length = 500)
    private String reason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
