package com.commerceops.erp.domain.shipping.controller;

import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.shipping.dto.CarrierActiveUpdateRequest;
import com.commerceops.erp.domain.shipping.dto.CarrierRequest;
import com.commerceops.erp.domain.shipping.dto.CarrierResponse;
import com.commerceops.erp.domain.shipping.dto.ShippingMethodActiveUpdateRequest;
import com.commerceops.erp.domain.shipping.dto.ShippingMethodRequest;
import com.commerceops.erp.domain.shipping.dto.ShippingMethodResponse;
import com.commerceops.erp.domain.shipping.service.ShippingSettingService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminShippingSettingController {

    private final ShippingSettingService shippingSettingService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/carriers")
    public ResponseEntity<ApiResponse<PageResponse<CarrierResponse>>> getCarriers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.CARRIER_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("택배사 목록을 조회했습니다.",
                shippingSettingService.getCarriers(keyword, active, page, size)));
    }

    @PostMapping("/carriers")
    public ResponseEntity<ApiResponse<CarrierResponse>> createCarrier(
            @Valid @RequestBody CarrierRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.CARRIER_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("택배사를 생성했습니다.",
                        shippingSettingService.createCarrier(request, userDetails.getUser())));
    }

    @PatchMapping("/carriers/{carrierId}")
    public ResponseEntity<ApiResponse<CarrierResponse>> updateCarrier(
            @PathVariable Long carrierId,
            @Valid @RequestBody CarrierRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.CARRIER_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("택배사를 수정했습니다.",
                shippingSettingService.updateCarrier(carrierId, request, userDetails.getUser())));
    }

    @PatchMapping("/carriers/{carrierId}/active")
    public ResponseEntity<ApiResponse<CarrierResponse>> updateCarrierActive(
            @PathVariable Long carrierId,
            @Valid @RequestBody CarrierActiveUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.CARRIER_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("택배사 활성 상태를 변경했습니다.",
                shippingSettingService.updateCarrierActive(carrierId, request.active(), userDetails.getUser())));
    }

    @GetMapping("/shipping-methods")
    public ResponseEntity<ApiResponse<PageResponse<ShippingMethodResponse>>> getShippingMethods(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long carrierId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.CARRIER_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("배송 방법 목록을 조회했습니다.",
                shippingSettingService.getShippingMethods(keyword, carrierId, active, page, size)));
    }

    @PostMapping("/shipping-methods")
    public ResponseEntity<ApiResponse<ShippingMethodResponse>> createShippingMethod(
            @Valid @RequestBody ShippingMethodRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.CARRIER_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("배송 방법을 생성했습니다.",
                        shippingSettingService.createShippingMethod(request, userDetails.getUser())));
    }

    @PatchMapping("/shipping-methods/{shippingMethodId}")
    public ResponseEntity<ApiResponse<ShippingMethodResponse>> updateShippingMethod(
            @PathVariable Long shippingMethodId,
            @Valid @RequestBody ShippingMethodRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.CARRIER_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("배송 방법을 수정했습니다.",
                shippingSettingService.updateShippingMethod(shippingMethodId, request, userDetails.getUser())));
    }

    @PatchMapping("/shipping-methods/{shippingMethodId}/active")
    public ResponseEntity<ApiResponse<ShippingMethodResponse>> updateShippingMethodActive(
            @PathVariable Long shippingMethodId,
            @Valid @RequestBody ShippingMethodActiveUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.CARRIER_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("배송 방법 활성 상태를 변경했습니다.",
                shippingSettingService.updateShippingMethodActive(shippingMethodId, request.active(), userDetails.getUser())));
    }
}
