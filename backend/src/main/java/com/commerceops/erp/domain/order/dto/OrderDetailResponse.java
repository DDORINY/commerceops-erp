package com.commerceops.erp.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String orderNumber,
        String receiverName,
        String receiverPhone,
        String address,
        String detailAddress,
        Integer totalPrice,
        String status,
        String paymentStatus,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {}
