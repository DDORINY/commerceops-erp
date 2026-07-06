package com.commerceops.erp.domain.coupon.controller;

import com.commerceops.erp.domain.coupon.dto.CouponCreateRequest;
import com.commerceops.erp.domain.coupon.dto.CouponResponse;
import com.commerceops.erp.domain.coupon.service.CouponService;
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
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.COUPON_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("쿠폰 목록 조회가 완료되었습니다.", couponService.getAllCoupons()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.COUPON_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("쿠폰이 등록되었습니다.", couponService.createCoupon(request)));
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<ApiResponse<Void>> deactivateCoupon(
            @PathVariable Long couponId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.COUPON_MANAGE);
        couponService.deactivateCoupon(couponId);
        return ResponseEntity.ok(ApiResponse.<Void>ok("쿠폰이 비활성화되었습니다.", null));
    }
}
