package com.commerceops.erp.domain.order.controller;

import com.commerceops.erp.domain.order.dto.AdminOrderResponse;
import com.commerceops.erp.domain.order.dto.OrderStatusUpdateRequest;
import com.commerceops.erp.domain.order.dto.OrderStatusUpdateResponse;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.service.OrderService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminOrderResponse>>> getAdminOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AdminOrderResponse> response = orderService.getAdminOrders(status, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.ok("관리자 주문 목록 조회가 완료되었습니다.", response));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.ok("주문 상태가 변경되었습니다.", response));
    }
}
