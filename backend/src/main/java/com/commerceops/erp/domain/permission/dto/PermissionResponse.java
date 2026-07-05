package com.commerceops.erp.domain.permission.dto;

import com.commerceops.erp.domain.permission.entity.Permission;

import java.time.LocalDateTime;

public record PermissionResponse(
        Long id,
        String code,
        String name,
        String domain,
        String action,
        String description,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDomain(),
                permission.getAction(),
                permission.getDescription(),
                permission.getActive(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
