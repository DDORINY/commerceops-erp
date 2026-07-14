package com.commerceops.erp.domain.address.entity;

import com.commerceops.erp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_addresses")
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class UserAddress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false, length = 50) private String addressName;
    @Column(nullable = false, length = 50) private String recipientName;
    @Column(nullable = false, length = 20) private String phone;
    @Column(nullable = false, length = 10) private String postalCode;
    @Column(nullable = false) private String roadAddress;
    private String detailAddress;
    private String extraAddress;
    private String deliveryRequest;
    @Column(nullable = false) private boolean isDefault;
    @CreatedDate @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(nullable = false) private LocalDateTime updatedAt;

    public void update(String addressName, String recipientName, String phone, String postalCode,
                       String roadAddress, String detailAddress, String extraAddress, String deliveryRequest) {
        this.addressName = addressName; this.recipientName = recipientName; this.phone = phone;
        this.postalCode = postalCode; this.roadAddress = roadAddress; this.detailAddress = detailAddress;
        this.extraAddress = extraAddress; this.deliveryRequest = deliveryRequest;
    }
    public void setDefault(boolean value) { this.isDefault = value; }
}
