package com.commerceops.erp.domain.banner.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final MainBannerService mainBannerService;
    private final PermissionChecker permissionChecker;

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
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("배너가 등록되었습니다.", mainBannerService.createBanner(request)));
    }

    @PatchMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<MainBannerResponse>> updateBanner(
            @PathVariable Long bannerId,
            @Valid @RequestBody MainBannerRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BANNER_MANAGE);
        return ResponseEntity.ok(
                ApiResponse.ok("배너가 수정되었습니다.", mainBannerService.updateBanner(bannerId, request))
        );
    }

    @DeleteMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(
            @PathVariable Long bannerId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BANNER_MANAGE);
        mainBannerService.deactivateBanner(bannerId);
        return ResponseEntity.ok(ApiResponse.noContent("배너가 비활성화되었습니다."));
    }
}
