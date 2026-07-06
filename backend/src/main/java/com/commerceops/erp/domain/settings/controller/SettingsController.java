package com.commerceops.erp.domain.settings.controller;

import com.commerceops.erp.domain.settings.dto.PublicBusinessSettingsResponse;
import com.commerceops.erp.domain.settings.dto.PublicTermsVersionResponse;
import com.commerceops.erp.domain.settings.enums.TermsType;
import com.commerceops.erp.domain.settings.service.SettingsService;
import com.commerceops.erp.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/company/public")
    public ResponseEntity<ApiResponse<PublicBusinessSettingsResponse>> getPublicCompanySettings() {
        return ResponseEntity.ok(ApiResponse.ok("공개 사업자 정보를 조회했습니다.", settingsService.getPublicCompanySettings()));
    }

    @GetMapping("/terms/{type}/latest")
    public ResponseEntity<ApiResponse<PublicTermsVersionResponse>> getPublicLatestTerms(@PathVariable TermsType type) {
        return ResponseEntity.ok(ApiResponse.ok("공개 약관/정책을 조회했습니다.", settingsService.getPublicLatestTerms(type)));
    }
}
