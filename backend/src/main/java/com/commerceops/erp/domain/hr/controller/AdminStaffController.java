package com.commerceops.erp.domain.hr.controller;

import com.commerceops.erp.domain.hr.dto.StaffActiveUpdateRequest;
import com.commerceops.erp.domain.hr.dto.StaffCreateRequest;
import com.commerceops.erp.domain.hr.dto.StaffProfileResponse;
import com.commerceops.erp.domain.hr.dto.StaffStatusUpdateRequest;
import com.commerceops.erp.domain.hr.dto.StaffUpdateRequest;
import com.commerceops.erp.domain.hr.enums.EmploymentStatus;
import com.commerceops.erp.domain.hr.service.AdminStaffService;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StaffProfileResponse>>> getStaff(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) EmploymentStatus employmentStatus,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok("직원 목록 조회가 완료되었습니다.",
                adminStaffService.getStaff(keyword, departmentId, positionId, employmentStatus, active, role, page, size)));
    }

    @GetMapping("/{staffId}")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> getStaffDetail(@PathVariable Long staffId) {
        return ResponseEntity.ok(ApiResponse.ok("직원 상세 조회가 완료되었습니다.",
                adminStaffService.getStaffDetail(staffId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StaffProfileResponse>> createStaff(
            @Valid @RequestBody StaffCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("직원이 등록되었습니다.",
                        adminStaffService.createStaff(request, userDetails.getUser())));
    }

    @PatchMapping("/{staffId}")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> updateStaff(
            @PathVariable Long staffId,
            @Valid @RequestBody StaffUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("직원 정보가 수정되었습니다.",
                adminStaffService.updateStaff(staffId, request, userDetails.getUser())));
    }

    @PatchMapping("/{staffId}/status")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> updateEmploymentStatus(
            @PathVariable Long staffId,
            @Valid @RequestBody StaffStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("직원 재직 상태가 변경되었습니다.",
                adminStaffService.updateEmploymentStatus(staffId, request, userDetails.getUser())));
    }

    @PatchMapping("/{staffId}/active")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> updateActive(
            @PathVariable Long staffId,
            @Valid @RequestBody StaffActiveUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("직원 활성 상태가 변경되었습니다.",
                adminStaffService.updateActive(staffId, request, userDetails.getUser())));
    }
}
