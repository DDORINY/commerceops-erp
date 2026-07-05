package com.commerceops.erp.domain.hr.dto;

import com.commerceops.erp.domain.hr.enums.EmploymentStatus;
import com.commerceops.erp.domain.user.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StaffCreateRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 50) String name,
        @NotNull UserRole role,
        @Size(max = 50) String employeeNo,
        Long departmentId,
        Long positionId,
        EmploymentStatus employmentStatus,
        LocalDate joinedAt,
        Boolean active
) {
}
