package com.commerceops.erp.domain.order.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity @Table(name="order_addresses") @Getter @Builder
@NoArgsConstructor(access=AccessLevel.PROTECTED) @AllArgsConstructor(access=AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class OrderAddress {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id",nullable=false,unique=true) private Order order;
 @Column(nullable=false,length=50) private String recipientName;
 @Column(nullable=false,length=20) private String phone;
 @Column(nullable=false,length=10) private String postalCode;
 @Column(nullable=false) private String roadAddress;
 private String detailAddress; private String extraAddress; private String deliveryRequest;
 @CreatedDate @Column(nullable=false,updatable=false) private LocalDateTime createdAt;
}
