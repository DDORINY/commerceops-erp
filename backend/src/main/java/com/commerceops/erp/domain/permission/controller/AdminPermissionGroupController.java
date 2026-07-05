package com.commerceops.erp.domain.permission.controller;

import com.commerceops.erp.domain.permission.dto.PermissionGroupActiveUpdateRequest;
import com.commerceops.erp.domain.permission.dto.PermissionGroupCreateRequest;
import com.commerceops.erp.domain.permission.dto.PermissionGroupResponse;
import com.commerceops.erp.domain.permission.dto.PermissionGroupUpdateRequest;
import com.commerceops.erp.domain.permission.dto.UserPermissionGroupResponse;
import com.commerceops.erp.domain.permission.dto.UserPermissionGroupUpdateRequest;
import com.commerceops.erp.domain.permission.service.PermissionGroupService;
import com.commerceops.erp.global.response.ApiResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminPermissionGroupController {

    private final PermissionGroupService permissionGroupService;

    @GetMapping("/permission-groups")
    public ResponseEntity<ApiResponse<List<PermissionGroupResponse>>> getPermissionGroups() {
        return ResponseEntity.ok(ApiResponse.ok("권한 그룹 목록 조회가 완료되었습니다.", permissionGroupService.getPermissionGroups()));
    }

    @GetMapping("/permission-groups/{groupId}")
    public ResponseEntity<ApiResponse<PermissionGroupResponse>> getPermissionGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.ok("권한 그룹 상세 조회가 완료되었습니다.", permissionGroupService.getPermissionGroup(groupId)));
    }

    @PostMapping("/permission-groups")
    public ResponseEntity<ApiResponse<PermissionGroupResponse>> createPermissionGroup(
            @Valid @RequestBody PermissionGroupCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("권한 그룹을 생성했습니다.", permissionGroupService.createPermissionGroup(request, userDetails.getUser())));
    }

    @PatchMapping("/permission-groups/{groupId}")
    public ResponseEntity<ApiResponse<PermissionGroupResponse>> updatePermissionGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody PermissionGroupUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("권한 그룹 정보를 수정했습니다.", permissionGroupService.updatePermissionGroup(groupId, request, userDetails.getUser())));
    }

    @PatchMapping("/permission-groups/{groupId}/active")
    public ResponseEntity<ApiResponse<PermissionGroupResponse>> updatePermissionGroupActive(
            @PathVariable Long groupId,
            @Valid @RequestBody PermissionGroupActiveUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("권한 그룹 활성 상태를 변경했습니다.", permissionGroupService.updatePermissionGroupActive(groupId, request, userDetails.getUser())));
    }

    @GetMapping("/users/{userId}/permission-groups")
    public ResponseEntity<ApiResponse<List<UserPermissionGroupResponse>>> getUserPermissionGroups(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("사용자 권한 그룹 조회가 완료되었습니다.", permissionGroupService.getUserPermissionGroups(userId)));
    }

    @PutMapping("/users/{userId}/permission-groups")
    public ResponseEntity<ApiResponse<List<UserPermissionGroupResponse>>> updateUserPermissionGroups(
            @PathVariable Long userId,
            @RequestBody UserPermissionGroupUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("사용자 권한 그룹 할당을 변경했습니다.", permissionGroupService.updateUserPermissionGroups(userId, request, userDetails.getUser())));
    }
}
