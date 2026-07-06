package com.commerceops.erp.domain.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WarehouseLocationUpdateRequest(
        @NotBlank @Size(max = 60) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 60) String zone,
        @Size(max = 60) String aisle,
        @Size(max = 60) String rack,
        @Size(max = 60) String cell
) {
}
