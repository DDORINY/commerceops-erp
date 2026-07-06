package com.commerceops.erp.domain.inventory.controller;

import com.commerceops.erp.domain.inventory.dto.StockCountCreateRequest;
import com.commerceops.erp.domain.inventory.dto.StockCountItemsUpdateRequest;
import com.commerceops.erp.domain.inventory.dto.StockCountResponse;
import com.commerceops.erp.domain.inventory.enums.StockCountStatus;
import com.commerceops.erp.domain.inventory.service.StockCountService;
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
@RequestMapping("/api/admin/stock-counts")
@RequiredArgsConstructor
public class AdminStockCountController {

    private final StockCountService stockCountService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockCountResponse>>> getSessions(
            @RequestParam(required = false) StockCountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("재고 실사 목록을 조회했습니다.",
                stockCountService.getSessions(status, page, size)));
    }

    @GetMapping("/{stockCountId}")
    public ResponseEntity<ApiResponse<StockCountResponse>> getSession(
            @PathVariable Long stockCountId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("재고 실사를 조회했습니다.",
                stockCountService.getSession(stockCountId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockCountResponse>> create(
            @Valid @RequestBody StockCountCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.STOCK_COUNT_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("재고 실사 세션을 생성했습니다.",
                        stockCountService.create(request, userDetails.getUser())));
    }

    @PatchMapping("/{stockCountId}/items")
    public ResponseEntity<ApiResponse<StockCountResponse>> updateItems(
            @PathVariable Long stockCountId,
            @Valid @RequestBody StockCountItemsUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.STOCK_COUNT_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("재고 실사 품목을 저장했습니다.",
                stockCountService.updateItems(stockCountId, request)));
    }

    @PatchMapping("/{stockCountId}/start")
    public ResponseEntity<ApiResponse<StockCountResponse>> start(
            @PathVariable Long stockCountId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.STOCK_COUNT_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("재고 실사를 시작했습니다.",
                stockCountService.start(stockCountId, userDetails.getUser())));
    }

    @PatchMapping("/{stockCountId}/complete")
    public ResponseEntity<ApiResponse<StockCountResponse>> complete(
            @PathVariable Long stockCountId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.STOCK_COUNT_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("재고 실사를 완료했습니다.",
                stockCountService.complete(stockCountId, userDetails.getUser())));
    }

    @PatchMapping("/{stockCountId}/cancel")
    public ResponseEntity<ApiResponse<StockCountResponse>> cancel(
            @PathVariable Long stockCountId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.STOCK_COUNT_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("재고 실사를 취소했습니다.",
                stockCountService.cancel(stockCountId, userDetails.getUser())));
    }
}
