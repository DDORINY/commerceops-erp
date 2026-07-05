package com.commerceops.erp.domain.hr.controller;

import com.commerceops.erp.domain.hr.dto.DepartmentResponse;
import com.commerceops.erp.domain.hr.dto.PositionResponse;
import com.commerceops.erp.domain.hr.dto.StaffProfileResponse;
import com.commerceops.erp.domain.hr.service.DepartmentService;
import com.commerceops.erp.domain.hr.service.PositionService;
import com.commerceops.erp.domain.hr.service.StaffProfileService;
import com.commerceops.erp.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/hr")
@RequiredArgsConstructor
public class AdminHrController {

    private final DepartmentService departmentService;
    private final PositionService positionService;
    private final StaffProfileService staffProfileService;

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartments() {
        return ResponseEntity.ok(
                ApiResponse.ok("Departments loaded.", departmentService.getDepartments())
        );
    }

    @GetMapping("/positions")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getPositions() {
        return ResponseEntity.ok(
                ApiResponse.ok("Positions loaded.", positionService.getPositions())
        );
    }

    @GetMapping("/staff-profiles")
    public ResponseEntity<ApiResponse<List<StaffProfileResponse>>> getStaffProfiles() {
        return ResponseEntity.ok(
                ApiResponse.ok("Staff profiles loaded.", staffProfileService.getStaffProfiles())
        );
    }
}
