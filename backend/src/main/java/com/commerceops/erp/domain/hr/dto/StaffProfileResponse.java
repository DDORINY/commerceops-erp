package com.commerceops.erp.domain.hr.dto;

import com.commerceops.erp.domain.hr.entity.StaffProfile;
import com.commerceops.erp.domain.hr.enums.EmploymentStatus;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.domain.user.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StaffProfileResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        UserRole userRole,
        UserStatus userStatus,
        Long departmentId,
        String departmentName,
        Long positionId,
        String positionName,
        String employeeNo,
        EmploymentStatus employmentStatus,
        LocalDate joinedAt,
        LocalDate leftAt,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static StaffProfileResponse from(StaffProfile profile) {
        return new StaffProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getUser().getRole(),
                profile.getUser().getStatus(),
                profile.getDepartment() != null ? profile.getDepartment().getId() : null,
                profile.getDepartment() != null ? profile.getDepartment().getName() : null,
                profile.getPosition() != null ? profile.getPosition().getId() : null,
                profile.getPosition() != null ? profile.getPosition().getName() : null,
                profile.getEmployeeNo(),
                profile.getEmploymentStatus(),
                profile.getJoinedAt(),
                profile.getLeftAt(),
                profile.getActive(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
