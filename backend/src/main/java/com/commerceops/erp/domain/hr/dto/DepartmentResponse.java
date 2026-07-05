package com.commerceops.erp.domain.hr.dto;

import com.commerceops.erp.domain.hr.entity.Department;

import java.time.LocalDateTime;

public record DepartmentResponse(
        Long id,
        String name,
        String code,
        Long parentId,
        Integer sortOrder,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getParent() != null ? department.getParent().getId() : null,
                department.getSortOrder(),
                department.getActive(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
