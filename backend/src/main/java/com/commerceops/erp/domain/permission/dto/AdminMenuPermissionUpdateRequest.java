package com.commerceops.erp.domain.permission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminMenuPermissionUpdateRequest(
        @Valid List<Item> items
) {
    public record Item(
            @NotBlank String menuKey,
            @NotBlank String menuLabel,
            @NotBlank String menuPath,
            @NotBlank String requiredPermissionCode,
            @NotNull Boolean visible,
            Integer sortOrder
    ) {
    }
}
