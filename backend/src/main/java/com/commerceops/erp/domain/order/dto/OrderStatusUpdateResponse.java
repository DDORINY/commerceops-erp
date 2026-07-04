package com.commerceops.erp.domain.order.dto;

public record OrderStatusUpdateResponse(
        Long orderId,
        String status
) {}
