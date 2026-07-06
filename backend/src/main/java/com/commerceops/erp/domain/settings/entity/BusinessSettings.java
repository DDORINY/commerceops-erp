package com.commerceops.erp.domain.settings.entity;

import com.commerceops.erp.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_settings")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class BusinessSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String companyName;

    @Column(length = 80)
    private String representativeName;

    @Column(length = 30)
    private String businessRegistrationNumber;

    @Column(length = 60)
    private String mailOrderBusinessNumber;

    @Column(length = 500)
    private String address;

    @Column(length = 30)
    private String customerServicePhone;

    @Column(length = 120)
    private String customerServiceEmail;

    @Column(length = 120)
    private String brandName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(String companyName, String representativeName, String businessRegistrationNumber,
                       String mailOrderBusinessNumber, String address, String customerServicePhone,
                       String customerServiceEmail, String brandName, User updatedBy) {
        this.companyName = companyName;
        this.representativeName = representativeName;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.mailOrderBusinessNumber = mailOrderBusinessNumber;
        this.address = address;
        this.customerServicePhone = customerServicePhone;
        this.customerServiceEmail = customerServiceEmail;
        this.brandName = brandName;
        this.updatedBy = updatedBy;
    }
}
