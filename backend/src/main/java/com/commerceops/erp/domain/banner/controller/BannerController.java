package com.commerceops.erp.domain.banner.controller;

import com.commerceops.erp.domain.banner.dto.MainBannerResponse;
import com.commerceops.erp.domain.banner.service.MainBannerService;
import com.commerceops.erp.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final MainBannerService mainBannerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MainBannerResponse>>> getBanners() {
        return ResponseEntity.ok(
                ApiResponse.ok("배너 목록을 조회했습니다.", mainBannerService.getVisibleBanners())
        );
    }
}
