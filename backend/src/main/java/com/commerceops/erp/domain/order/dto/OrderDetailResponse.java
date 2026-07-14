package com.commerceops.erp.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.commerceops.erp.domain.payment.dto.PaymentSummaryResponse;

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
        PaymentSummaryResponse payment,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {}
