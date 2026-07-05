package com.commerceops.erp.domain.user.entity;

import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static User create(String email, String encodedPassword, String name, String phone) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .phone(phone)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public static User createAdmin(String email, String encodedPassword, String name) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public void changeRole(UserRole newRole) {
        this.role = newRole;
    }

    public void updateProfile(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
    }

    public void changeStatus(UserStatus newStatus) {
        this.status = newStatus;
    }
}
