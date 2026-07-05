package com.commerceops.erp.domain.permission.dto;

import jakarta.validation.constraints.NotNull;

public record PermissionGroupActiveUpdateRequest(
        @NotNull Boolean active
) {
}
