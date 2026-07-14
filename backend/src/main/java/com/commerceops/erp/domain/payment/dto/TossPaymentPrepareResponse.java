package com.commerceops.erp.domain.payment.dto;

public record TossPaymentPrepareResponse(
        Long orderId, String paymentOrderId, String orderName, Integer amount,
        String customerKey, String customerName, String customerEmail
) {}
