package com.commerceops.erp.domain.inventory.controller;

import com.commerceops.erp.domain.inventory.dto.*;
import com.commerceops.erp.domain.inventory.enums.InventoryLogType;
import com.commerceops.erp.domain.inventory.service.InventoryService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> getInventoryList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean lowStockOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        PageResponse<InventoryResponse> response = inventoryService.getInventoryList(keyword, status, lowStockOnly, pageable);
        return ResponseEntity.ok(ApiResponse.ok("재고 목록 조회가 완료되었습니다.", response));
    }

    @PostMapping("/inbound")
    public ResponseEntity<ApiResponse<InventoryStockChangeResponse>> inbound(
            @Valid @RequestBody InventoryInboundRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_WRITE);
        InventoryStockChangeResponse response = inventoryService.inbound(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("입고 처리가 완료되었습니다.", response));
    }

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<InventoryStockChangeResponse>> adjust(
            @Valid @RequestBody InventoryAdjustRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_WRITE);
        InventoryStockChangeResponse response = inventoryService.adjust(request);
        return ResponseEntity.ok(ApiResponse.ok("재고 조정이 완료되었습니다.", response));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<PageResponse<InventoryLogResponse>>> getLogs(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) InventoryLogType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<InventoryLogResponse> response = inventoryService.getLogs(productId, type, pageable);
        return ResponseEntity.ok(ApiResponse.ok("재고 로그 조회가 완료되었습니다.", response));
    }
}
