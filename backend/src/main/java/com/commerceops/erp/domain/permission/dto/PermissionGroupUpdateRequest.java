package com.commerceops.erp.domain.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionGroupUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description
) {
}
