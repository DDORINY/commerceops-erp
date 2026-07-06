package com.commerceops.erp.domain.production.controller;

import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.production.dto.ProductionOrderCancelRequest;
import com.commerceops.erp.domain.production.dto.ProductionOrderCompleteRequest;
import com.commerceops.erp.domain.production.dto.ProductionOrderCreateRequest;
import com.commerceops.erp.domain.production.dto.ProductionOrderListResponse;
import com.commerceops.erp.domain.production.dto.ProductionOrderResponse;
import com.commerceops.erp.domain.production.dto.ProductionOrderStartRequest;
import com.commerceops.erp.domain.production.dto.ProductionOrderUpdateRequest;
import com.commerceops.erp.domain.production.dto.ProductionReceiptResponse;
import com.commerceops.erp.domain.production.enums.ProductionOrderStatus;
import com.commerceops.erp.domain.production.service.ProductionService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductionController {

    private final ProductionService productionService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/production-orders")
    public ResponseEntity<ApiResponse<PageResponse<ProductionOrderListResponse>>> getOrders(
            @RequestParam(required = false) ProductionOrderStatus status,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("생산 주문 목록을 조회했습니다.",
                productionService.getOrders(status, warehouseId, skuId, keyword, dateFrom, dateTo, page, size)));
    }

    @GetMapping("/production-orders/{productionOrderId}")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> getOrder(
            @PathVariable Long productionOrderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("생산 주문 상세를 조회했습니다.",
                productionService.getOrder(productionOrderId)));
    }

    @PostMapping("/production-orders")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> createOrder(
            @Valid @RequestBody ProductionOrderCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCTION_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("생산 주문을 생성했습니다.",
                        productionService.createOrder(request, userDetails.getUser())));
    }

    @PatchMapping("/production-orders/{productionOrderId}")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> updateOrder(
            @PathVariable Long productionOrderId,
            @Valid @RequestBody ProductionOrderUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCTION_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("생산 주문을 수정했습니다.",
                productionService.updateOrder(productionOrderId, request, userDetails.getUser())));
    }

    @PatchMapping("/production-orders/{productionOrderId}/start")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> startOrder(
            @PathVariable Long productionOrderId,
            @RequestBody(required = false) ProductionOrderStartRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCTION_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("생산 주문을 시작했습니다.",
                productionService.startOrder(productionOrderId, userDetails.getUser())));
    }

    @PatchMapping("/production-orders/{productionOrderId}/complete")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> completeOrder(
            @PathVariable Long productionOrderId,
            @Valid @RequestBody ProductionOrderCompleteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCTION_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("생산 주문을 완료 처리했습니다.",
                productionService.completeOrder(productionOrderId, request, userDetails.getUser())));
    }

    @PatchMapping("/production-orders/{productionOrderId}/cancel")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> cancelOrder(
            @PathVariable Long productionOrderId,
            @RequestBody(required = false) ProductionOrderCancelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCTION_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("생산 주문을 취소했습니다.",
                productionService.cancelOrder(productionOrderId, request, userDetails.getUser())));
    }

    @GetMapping("/production-receipts")
    public ResponseEntity<ApiResponse<PageResponse<ProductionReceiptResponse>>> getReceipts(
            @RequestParam(required = false) Long productionOrderId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("생산 입고 이력을 조회했습니다.",
                productionService.getReceipts(productionOrderId, skuId, warehouseId, page, size)));
    }

    @GetMapping("/production-receipts/{receiptId}")
    public ResponseEntity<ApiResponse<ProductionReceiptResponse>> getReceipt(
            @PathVariable Long receiptId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("생산 입고 상세를 조회했습니다.",
                productionService.getReceipt(receiptId)));
    }
}
