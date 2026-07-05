package com.commerceops.erp.domain.permission.dto;

import com.commerceops.erp.domain.permission.entity.UserPermissionGroup;

import java.time.LocalDateTime;

public record UserPermissionGroupResponse(
        Long assignmentId,
        Long userId,
        Long permissionGroupId,
        String permissionGroupName,
        String permissionGroupCode,
        Boolean systemGroup,
        Boolean active,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static UserPermissionGroupResponse from(UserPermissionGroup assignment) {
        return new UserPermissionGroupResponse(
                assignment.getId(),
                assignment.getUser().getId(),
                assignment.getPermissionGroup().getId(),
                assignment.getPermissionGroup().getName(),
                assignment.getPermissionGroup().getCode(),
                assignment.getPermissionGroup().getSystemGroup(),
                assignment.getPermissionGroup().getActive(),
                assignment.getCreatedBy(),
                assignment.getCreatedAt()
        );
    }
}
