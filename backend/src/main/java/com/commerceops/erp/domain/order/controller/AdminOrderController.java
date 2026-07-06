package com.commerceops.erp.domain.order.controller;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.order.dto.AdminOrderResponse;
import com.commerceops.erp.domain.order.dto.OrderStatusUpdateRequest;
import com.commerceops.erp.domain.order.dto.OrderStatusUpdateResponse;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.service.OrderService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminOrderResponse>>> getAdminOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.ORDER_READ);
        PageResponse<AdminOrderResponse> response = orderService.getAdminOrders(status, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.ok("관리자 주문 목록 조회가 완료되었습니다.", response));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.ORDER_STATUS_CHANGE);
        OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderId, request);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.ORDER_STATUS_CHANGED,
                "ORDER",
                orderId,
                null,
                response.status(),
                "주문 상태를 변경했습니다."
        );
        return ResponseEntity.ok(ApiResponse.ok("주문 상태가 변경되었습니다.", response));
    }
}
