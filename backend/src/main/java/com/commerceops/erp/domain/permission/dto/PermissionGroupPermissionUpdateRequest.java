package com.commerceops.erp.domain.permission.dto;

import java.util.List;

public record PermissionGroupPermissionUpdateRequest(
        List<Long> permissionIds
) {
}
