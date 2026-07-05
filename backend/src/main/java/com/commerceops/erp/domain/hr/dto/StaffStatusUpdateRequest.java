package com.commerceops.erp.domain.hr.dto;

import com.commerceops.erp.domain.hr.enums.EmploymentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StaffStatusUpdateRequest(
        @NotNull EmploymentStatus employmentStatus,
        LocalDate leftAt
) {
}
