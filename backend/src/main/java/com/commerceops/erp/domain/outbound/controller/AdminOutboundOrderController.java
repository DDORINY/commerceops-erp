package com.commerceops.erp.domain.outbound.controller;

import com.commerceops.erp.domain.outbound.dto.OutboundOrderCreateRequest;
import com.commerceops.erp.domain.outbound.dto.OutboundOrderResponse;
import com.commerceops.erp.domain.outbound.dto.OutboundOrderUpdateRequest;
import com.commerceops.erp.domain.outbound.enums.OutboundOrderStatus;
import com.commerceops.erp.domain.outbound.service.OutboundOrderService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
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
@RequestMapping("/api/admin/outbound-orders")
@RequiredArgsConstructor
public class AdminOutboundOrderController {

    private final OutboundOrderService outboundOrderService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OutboundOrderResponse>>> getOutboundOrders(
            @RequestParam(required = false) OutboundOrderStatus status,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.OUTBOUND_READ);
        return ResponseEntity.ok(ApiResponse.ok("출고 지시 목록을 조회했습니다.",
                outboundOrderService.getOutboundOrders(status, warehouseId, orderId, keyword, page, size)));
    }

    @GetMapping("/{outboundOrderId}")
    public ResponseEntity<ApiResponse<OutboundOrderResponse>> getOutboundOrder(
            @PathVariable Long outboundOrderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.OUTBOUND_READ);
        return ResponseEntity.ok(ApiResponse.ok("출고 지시 상세를 조회했습니다.",
                outboundOrderService.getOutboundOrder(outboundOrderId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OutboundOrderResponse>> createOutboundOrder(
            @Valid @RequestBody OutboundOrderCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.OUTBOUND_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("출고 지시를 생성했습니다.",
                        outboundOrderService.createOutboundOrder(request, userDetails.getUser())));
    }

    @PatchMapping("/{outboundOrderId}")
    public ResponseEntity<ApiResponse<OutboundOrderResponse>> updateOutboundOrder(
            @PathVariable Long outboundOrderId,
            @Valid @RequestBody OutboundOrderUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.OUTBOUND_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("출고 지시를 수정했습니다.",
                outboundOrderService.updateOutboundOrder(outboundOrderId, request, userDetails.getUser())));
    }

    @PatchMapping("/{outboundOrderId}/pick")
    public ResponseEntity<ApiResponse<OutboundOrderResponse>> pickOutboundOrder(
            @PathVariable Long outboundOrderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.OUTBOUND_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("출고 지시를 피킹 완료 처리했습니다.",
                outboundOrderService.pickOutboundOrder(outboundOrderId, userDetails.getUser())));
    }

    @PatchMapping("/{outboundOrderId}/cancel")
    public ResponseEntity<ApiResponse<OutboundOrderResponse>> cancelOutboundOrder(
            @PathVariable Long outboundOrderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.OUTBOUND_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("출고 지시를 취소했습니다.",
                outboundOrderService.cancelOutboundOrder(outboundOrderId, userDetails.getUser())));
    }
}
