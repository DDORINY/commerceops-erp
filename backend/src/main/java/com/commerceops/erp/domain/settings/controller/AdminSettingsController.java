package com.commerceops.erp.domain.settings.controller;

import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.settings.dto.BusinessSettingsResponse;
import com.commerceops.erp.domain.settings.dto.BusinessSettingsUpdateRequest;
import com.commerceops.erp.domain.settings.dto.TermsVersionCreateRequest;
import com.commerceops.erp.domain.settings.dto.TermsVersionResponse;
import com.commerceops.erp.domain.settings.enums.TermsType;
import com.commerceops.erp.domain.settings.service.SettingsService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final SettingsService settingsService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/company")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> getCompanySettings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SETTINGS_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("사업자 설정을 조회했습니다.", settingsService.getCompanySettings()));
    }

    @PutMapping("/company")
    public ResponseEntity<ApiResponse<BusinessSettingsResponse>> updateCompanySettings(
            @Valid @RequestBody BusinessSettingsUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SETTINGS_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok(
                "사업자 설정이 저장되었습니다.",
                settingsService.updateCompanySettings(request, userDetails.getUser())
        ));
    }

    @GetMapping("/terms")
    public ResponseEntity<ApiResponse<List<TermsVersionResponse>>> getTerms(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SETTINGS_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("약관/정책 버전 목록을 조회했습니다.", settingsService.getTermsVersions()));
    }

    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<TermsVersionResponse>> createTerms(
            @Valid @RequestBody TermsVersionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SETTINGS_MANAGE);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "약관/정책 새 버전이 생성되었습니다.",
                        settingsService.createTermsVersion(request, userDetails.getUser())
                ));
    }

    @GetMapping("/terms/{type}/latest")
    public ResponseEntity<ApiResponse<TermsVersionResponse>> getLatestTerms(
            @PathVariable TermsType type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SETTINGS_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("최신 약관/정책을 조회했습니다.", settingsService.getLatestTerms(type)));
    }

    @GetMapping("/terms/{type}/versions")
    public ResponseEntity<ApiResponse<List<TermsVersionResponse>>> getTermsVersions(
            @PathVariable TermsType type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SETTINGS_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("약관/정책 버전 이력을 조회했습니다.", settingsService.getTermsVersions(type)));
    }

    @GetMapping("/terms/{type}/versions/{versionId}")
    public ResponseEntity<ApiResponse<TermsVersionResponse>> getTermsVersion(
            @PathVariable TermsType type,
            @PathVariable Long versionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SETTINGS_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("약관/정책 버전 상세를 조회했습니다.", settingsService.getTermsVersion(type, versionId)));
    }
}
