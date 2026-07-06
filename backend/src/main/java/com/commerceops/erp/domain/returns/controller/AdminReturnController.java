package com.commerceops.erp.domain.returns.controller;

import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.returns.dto.ReturnAdminActionRequest;
import com.commerceops.erp.domain.returns.dto.ReturnResponse;
import com.commerceops.erp.domain.returns.enums.ReturnStatus;
import com.commerceops.erp.domain.returns.service.ReturnService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/returns")
@RequiredArgsConstructor
public class AdminReturnController {

    private final ReturnService returnService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ApiResponse<PageResponse<ReturnResponse>> getReturns(
            @RequestParam(required = false) ReturnStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ORDER_READ);
        return ApiResponse.ok(returnService.getAdminReturns(status, keyword, page, size));
    }

    @PatchMapping("/{id}/approve")
    public ApiResponse<ReturnResponse> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ReturnAdminActionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ORDER_STATUS_CHANGE);
        return ApiResponse.ok(returnService.approveReturn(id,
                request != null ? request : new ReturnAdminActionRequest(null)));
    }

    @PatchMapping("/{id}/reject")
    public ApiResponse<ReturnResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) ReturnAdminActionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ORDER_STATUS_CHANGE);
        return ApiResponse.ok(returnService.rejectReturn(id,
                request != null ? request : new ReturnAdminActionRequest(null)));
    }
}
