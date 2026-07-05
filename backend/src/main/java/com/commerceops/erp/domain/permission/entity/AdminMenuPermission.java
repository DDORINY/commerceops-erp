package com.commerceops.erp.domain.permission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "admin_menu_permissions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class AdminMenuPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_key", nullable = false, unique = true, length = 100)
    private String menuKey;

    @Column(name = "menu_label", nullable = false, length = 100)
    private String menuLabel;

    @Column(name = "menu_path", nullable = false, length = 200)
    private String menuPath;

    @Column(name = "required_permission_code", nullable = false, length = 100)
    private String requiredPermissionCode;

    @Builder.Default
    @Column(nullable = false)
    private Boolean visible = true;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(String menuLabel, String menuPath, String requiredPermissionCode, Boolean visible, Integer sortOrder) {
        this.menuLabel = menuLabel.trim();
        this.menuPath = menuPath.trim();
        this.requiredPermissionCode = requiredPermissionCode.trim().toUpperCase();
        this.visible = visible == null || visible;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}
