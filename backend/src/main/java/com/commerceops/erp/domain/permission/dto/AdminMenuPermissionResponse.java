package com.commerceops.erp.domain.permission.dto;

import com.commerceops.erp.domain.permission.entity.AdminMenuPermission;

import java.time.LocalDateTime;

public record AdminMenuPermissionResponse(
        Long id,
        String menuKey,
        String menuLabel,
        String menuPath,
        String requiredPermissionCode,
        Boolean visible,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminMenuPermissionResponse from(AdminMenuPermission menuPermission) {
        return new AdminMenuPermissionResponse(
                menuPermission.getId(),
                menuPermission.getMenuKey(),
                menuPermission.getMenuLabel(),
                menuPermission.getMenuPath(),
                menuPermission.getRequiredPermissionCode(),
                menuPermission.getVisible(),
                menuPermission.getSortOrder(),
                menuPermission.getCreatedAt(),
                menuPermission.getUpdatedAt()
        );
    }
}
