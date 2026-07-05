package com.commerceops.erp.domain.permission.dto;

import java.util.List;

public record UserPermissionGroupUpdateRequest(
        List<Long> permissionGroupIds
) {
}
