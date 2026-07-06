package com.commerceops.erp.domain.coupon.controller;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;

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
        CouponResponse response = couponService.createCoupon(request);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.COUPON_CREATED,
                "COUPON",
                response.id(),
                null,
                response.code(),
                "쿠폰을 등록했습니다: " + response.code()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("쿠폰이 등록되었습니다.", response));
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<ApiResponse<Void>> deactivateCoupon(
            @PathVariable Long couponId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.COUPON_MANAGE);
        couponService.deactivateCoupon(couponId);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.COUPON_DELETED,
                "COUPON",
                couponId,
                null,
                "inactive",
                "쿠폰을 비활성화했습니다."
        );
        return ResponseEntity.ok(ApiResponse.<Void>ok("쿠폰이 비활성화되었습니다.", null));
    }
}
