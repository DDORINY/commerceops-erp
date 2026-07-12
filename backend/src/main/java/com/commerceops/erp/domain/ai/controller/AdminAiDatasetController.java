package com.commerceops.erp.domain.ai.controller;

import com.commerceops.erp.domain.ai.dto.AiDatasetCatalogResponse;
import com.commerceops.erp.domain.ai.dto.AiDatasetExportResponse;
import com.commerceops.erp.domain.ai.enums.AiDatasetKey;
import com.commerceops.erp.domain.ai.service.AiDatasetExportService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ai/datasets")
@RequiredArgsConstructor
public class AdminAiDatasetController {

    private final AiDatasetExportService aiDatasetExportService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ApiResponse<List<AiDatasetCatalogResponse>> getCatalog(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_DATASET_EXPORT);
        return ApiResponse.ok(aiDatasetExportService.getCatalog());
    }

    @GetMapping("/{key}/export")
    public ApiResponse<AiDatasetExportResponse> export(
            @PathVariable AiDatasetKey key,
            @RequestParam(defaultValue = "100") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.AI_DATASET_EXPORT);
        return ApiResponse.ok(aiDatasetExportService.export(key, limit));
    }
}
