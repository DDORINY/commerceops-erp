package com.commerceops.erp.domain.hr.dto;

import com.commerceops.erp.domain.hr.enums.EmploymentStatus;
import com.commerceops.erp.domain.user.enums.UserRole;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StaffUpdateRequest(
        @Size(max = 50) String name,
        UserRole role,
        @Size(max = 50) String employeeNo,
        Long departmentId,
        Long positionId,
        EmploymentStatus employmentStatus,
        LocalDate joinedAt,
        LocalDate leftAt,
        Boolean active
) {
}
