package com.commerceops.erp.domain.warehouse.controller;

import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.warehouse.dto.*;
import com.commerceops.erp.domain.warehouse.enums.StockTransferStatus;
import com.commerceops.erp.domain.warehouse.service.WarehouseService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminWarehouseController {

    private final WarehouseService warehouseService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getWarehouses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.WAREHOUSE_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("창고 목록 조회가 완료되었습니다.", warehouseService.getWarehouses()));
    }

    @PostMapping("/warehouses")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @Valid @RequestBody WarehouseCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.WAREHOUSE_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("창고가 등록되었습니다.", warehouseService.createWarehouse(request)));
    }

    @GetMapping("/warehouse-stocks")
    public ResponseEntity<ApiResponse<PageResponse<WarehouseStockResponse>>> getWarehouseStocks(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok(
                "창고별 재고 조회가 완료되었습니다.",
                warehouseService.getWarehouseStocks(warehouseId, keyword, page, size)
        ));
    }

    @PostMapping("/warehouse-stocks/allocate")
    public ResponseEntity<ApiResponse<WarehouseStockResponse>> allocateStock(
            @Valid @RequestBody WarehouseStockAllocateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_WRITE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("재고가 창고에 배치되었습니다.", warehouseService.allocateStock(request)));
    }

    @GetMapping("/stock-transfers")
    public ResponseEntity<ApiResponse<PageResponse<StockTransferResponse>>> getStockTransfers(
            @RequestParam(required = false) StockTransferStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.WAREHOUSE_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok(
                "재고 이동 목록 조회가 완료되었습니다.",
                warehouseService.getStockTransfers(status, page, size)
        ));
    }

    @PostMapping("/stock-transfers")
    public ResponseEntity<ApiResponse<StockTransferResponse>> createStockTransfer(
            @Valid @RequestBody StockTransferCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.WAREHOUSE_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("재고 이동을 요청했습니다.", warehouseService.createStockTransfer(request)));
    }

    @PatchMapping("/stock-transfers/{id}/complete")
    public ResponseEntity<ApiResponse<StockTransferResponse>> completeStockTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.WAREHOUSE_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok(
                "재고 이동이 완료되었습니다.", warehouseService.completeStockTransfer(id)
        ));
    }
}
