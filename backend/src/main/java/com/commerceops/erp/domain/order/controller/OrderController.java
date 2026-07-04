package com.commerceops.erp.domain.order.controller;

import com.commerceops.erp.domain.order.dto.*;
import com.commerceops.erp.domain.order.service.OrderService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request) {
        OrderCreateResponse response = orderService.createOrder(userDetails.getUser(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("주문이 완료되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrderResponse> response = orderService.getOrders(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("주문 목록 조회가 완료되었습니다.", response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        OrderDetailResponse response = orderService.getOrderDetail(userDetails.getUser(), orderId);
        return ResponseEntity.ok(ApiResponse.ok("주문 상세 조회가 완료되었습니다.", response));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderStatusUpdateResponse>> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        OrderStatusUpdateResponse response = orderService.cancelOrder(userDetails.getUser(), orderId);
        return ResponseEntity.ok(ApiResponse.ok("주문이 취소되었습니다.", response));
    }
}
