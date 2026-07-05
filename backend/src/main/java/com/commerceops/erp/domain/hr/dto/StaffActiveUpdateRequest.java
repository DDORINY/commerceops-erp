package com.commerceops.erp.domain.hr.dto;

import jakarta.validation.constraints.NotNull;

public record StaffActiveUpdateRequest(
        @NotNull Boolean active
) {
}
