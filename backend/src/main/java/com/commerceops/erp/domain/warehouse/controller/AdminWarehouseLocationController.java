package com.commerceops.erp.domain.warehouse.controller;

import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationActiveRequest;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationCreateRequest;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationResponse;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationStockResponse;
import com.commerceops.erp.domain.warehouse.dto.WarehouseLocationUpdateRequest;
import com.commerceops.erp.domain.warehouse.service.WarehouseLocationService;
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
@RequestMapping("/api/admin/warehouse-locations")
@RequiredArgsConstructor
public class AdminWarehouseLocationController {

    private final WarehouseLocationService warehouseLocationService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WarehouseLocationResponse>>> getLocations(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok(
                "창고 위치 목록 조회가 완료되었습니다.",
                warehouseLocationService.getLocations(warehouseId, active, keyword, page, size)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseLocationResponse>> createLocation(
            @Valid @RequestBody WarehouseLocationCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.WAREHOUSE_MANAGE);
        WarehouseLocationResponse response = warehouseLocationService.createLocation(request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("창고 위치가 생성되었습니다.", response));
    }

    @PatchMapping("/{locationId}")
    public ResponseEntity<ApiResponse<WarehouseLocationResponse>> updateLocation(
            @PathVariable Long locationId,
            @Valid @RequestBody WarehouseLocationUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.WAREHOUSE_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok(
                "창고 위치가 수정되었습니다.",
                warehouseLocationService.updateLocation(locationId, request, userDetails.getUser())
        ));
    }

    @PatchMapping("/{locationId}/active")
    public ResponseEntity<ApiResponse<WarehouseLocationResponse>> changeActive(
            @PathVariable Long locationId,
            @Valid @RequestBody WarehouseLocationActiveRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.WAREHOUSE_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok(
                "창고 위치 활성 상태가 변경되었습니다.",
                warehouseLocationService.changeActive(locationId, request, userDetails.getUser())
        ));
    }

    @GetMapping("/{locationId}/stocks")
    public ResponseEntity<ApiResponse<PageResponse<WarehouseLocationStockResponse>>> getLocationStocks(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok(
                "창고 위치별 재고 조회가 완료되었습니다.",
                warehouseLocationService.getLocationStocks(locationId, page, size)
        ));
    }
}
