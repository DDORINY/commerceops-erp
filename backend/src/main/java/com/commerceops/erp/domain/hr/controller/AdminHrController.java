package com.commerceops.erp.domain.hr.controller;

import com.commerceops.erp.domain.hr.dto.DepartmentResponse;
import com.commerceops.erp.domain.hr.dto.PositionResponse;
import com.commerceops.erp.domain.hr.dto.StaffProfileResponse;
import com.commerceops.erp.domain.hr.service.DepartmentService;
import com.commerceops.erp.domain.hr.service.PositionService;
import com.commerceops.erp.domain.hr.service.StaffProfileService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final PermissionChecker permissionChecker;

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.STAFF_MANAGE);
        return ResponseEntity.ok(
                ApiResponse.ok("부서 목록 조회가 완료되었습니다.", departmentService.getDepartments())
        );
    }

    @GetMapping("/positions")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getPositions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.STAFF_MANAGE);
        return ResponseEntity.ok(
                ApiResponse.ok("직급 목록 조회가 완료되었습니다.", positionService.getPositions())
        );
    }

    @GetMapping("/staff-profiles")
    public ResponseEntity<ApiResponse<List<StaffProfileResponse>>> getStaffProfiles(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.STAFF_MANAGE);
        return ResponseEntity.ok(
                ApiResponse.ok("직원 프로필 조회가 완료되었습니다.", staffProfileService.getStaffProfiles())
        );
    }
}
