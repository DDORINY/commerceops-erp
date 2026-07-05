package com.commerceops.erp.domain.permission.dto;

import java.util.List;

public record EffectivePermissionResponse(
        Long userId,
        String userRole,
        List<String> permissionCodes
) {
}
