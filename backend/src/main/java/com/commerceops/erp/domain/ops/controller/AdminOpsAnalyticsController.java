package com.commerceops.erp.domain.ops.controller;

import com.commerceops.erp.domain.ops.dto.OpsAnalyticsOverviewResponse;
import com.commerceops.erp.domain.ops.service.OpsAnalyticsService;
import com.commerceops.erp.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ops-analytics")
@RequiredArgsConstructor
public class AdminOpsAnalyticsController {

    private final OpsAnalyticsService opsAnalyticsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<OpsAnalyticsOverviewResponse>> getOverview() {
        return ResponseEntity.ok(
                ApiResponse.ok("운영 분석 기초 지표 조회가 완료되었습니다.", opsAnalyticsService.getOverview()));
    }
}
