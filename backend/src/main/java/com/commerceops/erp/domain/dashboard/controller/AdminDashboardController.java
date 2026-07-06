package com.commerceops.erp.domain.dashboard.controller;

import com.commerceops.erp.domain.dashboard.dto.*;
import com.commerceops.erp.domain.dashboard.service.DashboardService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.DASHBOARD_READ);
        return ResponseEntity.ok(
                ApiResponse.ok("대시보드 요약 조회가 완료되었습니다.", dashboardService.getSummary()));
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<List<SalesResponse>>> getSales(
            @RequestParam(defaultValue = "DAILY") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ResponseEntity.ok(
                ApiResponse.ok("매출 통계 조회가 완료되었습니다.", dashboardService.getSales(period, startDate, endDate)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockProductResponse>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(
                ApiResponse.ok("재고 부족 상품 조회가 완료되었습니다.", dashboardService.getLowStockProducts(limit)));
    }

    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<TopProductResponse>>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_READ);
        return ResponseEntity.ok(
                ApiResponse.ok("인기 상품 조회가 완료되었습니다.", dashboardService.getTopProducts(limit)));
    }
}
