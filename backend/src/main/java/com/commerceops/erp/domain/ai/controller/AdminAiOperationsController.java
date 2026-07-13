package com.commerceops.erp.domain.ai.controller;

import com.commerceops.erp.domain.ai.dto.AiOperationsHealthResponse;
import com.commerceops.erp.domain.ai.dto.AiOperationsOverviewResponse;
import com.commerceops.erp.domain.ai.service.AiOperationsService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
public class AdminAiOperationsController {

    private final AiOperationsService aiOperationsService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/overview")
    public ApiResponse<AiOperationsOverviewResponse> getOverview(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_REPORT_READ);
        return ApiResponse.ok(aiOperationsService.getOverview());
    }

    @GetMapping("/health")
    public ApiResponse<AiOperationsHealthResponse> getHealth(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_REPORT_READ);
        return ApiResponse.ok(aiOperationsService.getHealth());
    }
}
