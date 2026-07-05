package com.commerceops.erp.domain.permission.controller;

import com.commerceops.erp.domain.permission.dto.AdminMenuPermissionResponse;
import com.commerceops.erp.domain.permission.dto.AdminMenuPermissionUpdateRequest;
import com.commerceops.erp.domain.permission.dto.EffectivePermissionResponse;
import com.commerceops.erp.domain.permission.dto.PermissionGroupPermissionUpdateRequest;
import com.commerceops.erp.domain.permission.dto.PermissionResponse;
import com.commerceops.erp.domain.permission.service.PermissionMatrixService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminPermissionMatrixController {

    private final PermissionMatrixService permissionMatrixService;

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions() {
        return ResponseEntity.ok(ApiResponse.ok("권한 목록 조회가 완료되었습니다.", permissionMatrixService.getPermissions()));
    }

    @GetMapping("/permission-groups/{groupId}/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getGroupPermissions(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.ok("권한 그룹별 권한 조회가 완료되었습니다.", permissionMatrixService.getGroupPermissions(groupId)));
    }

    @PutMapping("/permission-groups/{groupId}/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> updateGroupPermissions(
            @PathVariable Long groupId,
            @RequestBody PermissionGroupPermissionUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("권한 그룹별 권한 매핑을 저장했습니다.", permissionMatrixService.updateGroupPermissions(groupId, request, userDetails.getUser())));
    }

    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<ApiResponse<EffectivePermissionResponse>> getUserPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("사용자 effective permission 조회가 완료되었습니다.", permissionMatrixService.getEffectivePermissions(userId)));
    }

    @GetMapping("/menu-permissions")
    public ResponseEntity<ApiResponse<List<AdminMenuPermissionResponse>>> getMenuPermissions() {
        return ResponseEntity.ok(ApiResponse.ok("관리자 메뉴 권한 조회가 완료되었습니다.", permissionMatrixService.getMenuPermissions()));
    }

    @PutMapping("/menu-permissions")
    public ResponseEntity<ApiResponse<List<AdminMenuPermissionResponse>>> updateMenuPermissions(
            @Valid @RequestBody AdminMenuPermissionUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("관리자 메뉴 권한을 저장했습니다.", permissionMatrixService.updateMenuPermissions(request, userDetails.getUser())));
    }
}
