package com.commerceops.erp.domain.coupon.controller;

import com.commerceops.erp.domain.coupon.dto.CouponValidateResponse;
import com.commerceops.erp.domain.coupon.service.CouponService;
import com.commerceops.erp.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CouponValidateResponse>> validate(
            @RequestParam String code,
            @RequestParam int orderAmount) {
        return ResponseEntity.ok(
                ApiResponse.ok("쿠폰 유효성 확인이 완료되었습니다.", couponService.validate(code, orderAmount))
        );
    }
}
