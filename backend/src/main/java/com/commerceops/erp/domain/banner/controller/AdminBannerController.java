package com.commerceops.erp.domain.banner.controller;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.banner.dto.MainBannerRequest;
import com.commerceops.erp.domain.banner.dto.MainBannerResponse;
import com.commerceops.erp.domain.banner.service.MainBannerService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final MainBannerService mainBannerService;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MainBannerResponse>>> getBanners(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BANNER_MANAGE);
        return ResponseEntity.ok(
                ApiResponse.ok("관리자 배너 목록을 조회했습니다.", mainBannerService.getAdminBanners())
        );
    }

    @GetMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<MainBannerResponse>> getBanner(
            @PathVariable Long bannerId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BANNER_MANAGE);
        return ResponseEntity.ok(
                ApiResponse.ok("관리자 배너 상세를 조회했습니다.", mainBannerService.getAdminBanner(bannerId))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MainBannerResponse>> createBanner(
            @Valid @RequestBody MainBannerRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BANNER_MANAGE);
        MainBannerResponse response = mainBannerService.createBanner(request);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.BANNER_CREATED,
                "BANNER",
                response.id(),
                null,
                response.title(),
                "배너를 등록했습니다: " + response.title()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("배너가 등록되었습니다.", response));
    }

    @PatchMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<MainBannerResponse>> updateBanner(
            @PathVariable Long bannerId,
            @Valid @RequestBody MainBannerRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BANNER_MANAGE);
        MainBannerResponse response = mainBannerService.updateBanner(bannerId, request);
        auditLogService.record(
                userDetails.getUser(),
                Boolean.TRUE.equals(response.active()) ? AuditActionType.BANNER_UPDATED : AuditActionType.BANNER_ACTIVE_CHANGED,
                "BANNER",
                bannerId,
                null,
                response.title(),
                "배너를 수정했습니다: " + response.title()
        );
        return ResponseEntity.ok(ApiResponse.ok("배너가 수정되었습니다.", response));
    }

    @DeleteMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(
            @PathVariable Long bannerId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BANNER_MANAGE);
        mainBannerService.deactivateBanner(bannerId);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.BANNER_ACTIVE_CHANGED,
                "BANNER",
                bannerId,
                null,
                "inactive",
                "배너를 비활성화했습니다."
        );
        return ResponseEntity.ok(ApiResponse.noContent("배너가 비활성화되었습니다."));
    }
}
