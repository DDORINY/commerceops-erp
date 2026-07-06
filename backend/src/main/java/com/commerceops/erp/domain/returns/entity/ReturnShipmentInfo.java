package com.commerceops.erp.domain.returns.entity;

import com.commerceops.erp.domain.returns.enums.ReturnShipmentStatus;
import com.commerceops.erp.domain.returns.enums.ReturnShippingFeePayer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "return_shipment_infos",
        indexes = {
                @Index(name = "idx_return_shipment_infos_return_id", columnList = "return_request_id"),
                @Index(name = "idx_return_shipment_infos_tracking_number", columnList = "tracking_number"),
                @Index(name = "idx_return_shipment_infos_status", columnList = "status")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ReturnShipmentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_request_id", nullable = false, unique = true)
    private ReturnRequest returnRequest;

    @Column(length = 100)
    private String carrier;

    @Column(length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnShipmentStatus status;

    @Column(precision = 15, scale = 2)
    private BigDecimal shippingFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnShippingFeePayer feePayer;

    @Column(length = 500)
    private String memo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(String carrier, String trackingNumber, ReturnShipmentStatus status,
                       BigDecimal shippingFee, ReturnShippingFeePayer feePayer, String memo) {
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.shippingFee = shippingFee;
        this.feePayer = feePayer;
        this.memo = memo;
    }
}
