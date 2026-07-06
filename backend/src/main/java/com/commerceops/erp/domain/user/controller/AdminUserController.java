package com.commerceops.erp.domain.user.controller;

import com.commerceops.erp.domain.user.dto.UserSummaryResponse;
import com.commerceops.erp.domain.user.service.AdminUserService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserSummaryResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.ok("고객 목록 조회가 완료되었습니다.", adminUserService.getUsers(keyword, page, size))
        );
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> changeRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.ROLE_MANAGE);
        String role = body.get("role");
        return ResponseEntity.ok(
                ApiResponse.ok("역할이 변경되었습니다.", adminUserService.changeUserRole(userId, role))
        );
    }
}
