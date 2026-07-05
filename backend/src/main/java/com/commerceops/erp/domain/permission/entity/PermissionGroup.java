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
@Table(name = "permission_groups")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class PermissionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(name = "system_group", nullable = false)
    private Boolean systemGroup = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static PermissionGroup create(String name, String code, String description, Boolean systemGroup, Boolean active) {
        return PermissionGroup.builder()
                .name(name.trim())
                .code(normalizeCode(code))
                .description(normalizeBlank(description))
                .systemGroup(systemGroup != null && systemGroup)
                .active(active == null || active)
                .build();
    }

    public void update(String name, String description) {
        this.name = name.trim();
        this.description = normalizeBlank(description);
    }

    public void changeActive(Boolean active) {
        this.active = active;
    }

    public static String normalizeCode(String code) {
        return code.trim().toUpperCase().replaceAll("[^A-Z0-9_]+", "_");
    }

    private static String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
