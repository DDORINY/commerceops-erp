package com.commerceops.erp.domain.ai.controller;

import com.commerceops.erp.domain.ai.dto.AiOperationsHealthResponse;
import com.commerceops.erp.domain.ai.dto.AiOperationsOverviewResponse;
import com.commerceops.erp.domain.ai.dto.AiInsightResponse;
import com.commerceops.erp.domain.ai.service.AiOperationsService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/recommendations/products")
    public ApiResponse<List<AiInsightResponse>> getProductRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_RECOMMENDATION_READ);
        return ApiResponse.ok(aiOperationsService.getProductRecommendations(limit));
    }

    @GetMapping("/forecasts/demand")
    public ApiResponse<List<AiInsightResponse>> getDemandForecasts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_FORECAST_READ);
        return ApiResponse.ok(aiOperationsService.getDemandForecasts(limit));
    }

    @GetMapping("/reviews/analysis")
    public ApiResponse<List<AiInsightResponse>> getReviewAnalyses(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_REVIEW_ANALYSIS_READ);
        return ApiResponse.ok(aiOperationsService.getReviewAnalyses(limit));
    }

    @GetMapping("/anomalies/orders")
    public ApiResponse<List<AiInsightResponse>> getOrderAnomalies(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_ANOMALY_READ);
        return ApiResponse.ok(aiOperationsService.getOrderAnomalies(limit));
    }

    @GetMapping("/risks/inventory")
    public ApiResponse<List<AiInsightResponse>> getInventoryRiskAlerts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_RISK_ALERT_READ);
        return ApiResponse.ok(aiOperationsService.getInventoryRiskAlerts(limit));
    }

    @GetMapping("/risks/settlement")
    public ApiResponse<List<AiInsightResponse>> getSettlementRiskAlerts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_RISK_ALERT_READ);
        return ApiResponse.ok(aiOperationsService.getSettlementRiskAlerts(limit));
    }
}
