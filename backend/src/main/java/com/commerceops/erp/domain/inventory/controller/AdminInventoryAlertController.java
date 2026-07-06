package com.commerceops.erp.domain.inventory.controller;

import com.commerceops.erp.domain.inventory.dto.InventoryAlertRuleActiveRequest;
import com.commerceops.erp.domain.inventory.dto.InventoryAlertRuleRequest;
import com.commerceops.erp.domain.inventory.dto.InventoryAlertRuleResponse;
import com.commerceops.erp.domain.inventory.dto.LowStockAlertResponse;
import com.commerceops.erp.domain.inventory.service.InventoryAlertService;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminInventoryAlertController {

    private final InventoryAlertService inventoryAlertService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/inventory-alert-rules")
    public ResponseEntity<ApiResponse<PageResponse<InventoryAlertRuleResponse>>> getRules(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("안전재고 기준 목록 조회가 완료되었습니다.",
                inventoryAlertService.getRules(warehouseId, active, keyword, page, size)));
    }

    @PostMapping("/inventory-alert-rules")
    public ResponseEntity<ApiResponse<InventoryAlertRuleResponse>> createRule(
            @Valid @RequestBody InventoryAlertRuleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_WRITE);
        InventoryAlertRuleResponse response = inventoryAlertService.createRule(request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("안전재고 기준이 생성되었습니다.", response));
    }

    @PatchMapping("/inventory-alert-rules/{ruleId}")
    public ResponseEntity<ApiResponse<InventoryAlertRuleResponse>> updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody InventoryAlertRuleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_WRITE);
        return ResponseEntity.ok(ApiResponse.ok("안전재고 기준이 수정되었습니다.",
                inventoryAlertService.updateRule(ruleId, request, userDetails.getUser())));
    }

    @PatchMapping("/inventory-alert-rules/{ruleId}/active")
    public ResponseEntity<ApiResponse<InventoryAlertRuleResponse>> changeActive(
            @PathVariable Long ruleId,
            @Valid @RequestBody InventoryAlertRuleActiveRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_WRITE);
        return ResponseEntity.ok(ApiResponse.ok("안전재고 기준 활성 상태가 변경되었습니다.",
                inventoryAlertService.changeActive(ruleId, request, userDetails.getUser())));
    }

    @GetMapping("/inventory-alerts/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockAlertResponse>>> getLowStockAlerts(
            @RequestParam(required = false) Long warehouseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("재고 부족 알림 조회가 완료되었습니다.",
                inventoryAlertService.getLowStockAlerts(warehouseId)));
    }
}
