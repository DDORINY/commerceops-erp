package com.commerceops.erp.domain.permission.dto;

import com.commerceops.erp.domain.permission.entity.PermissionGroup;

import java.time.LocalDateTime;

public record PermissionGroupResponse(
        Long id,
        String name,
        String code,
        String description,
        Boolean systemGroup,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PermissionGroupResponse from(PermissionGroup group) {
        return new PermissionGroupResponse(
                group.getId(),
                group.getName(),
                group.getCode(),
                group.getDescription(),
                group.getSystemGroup(),
                group.getActive(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
