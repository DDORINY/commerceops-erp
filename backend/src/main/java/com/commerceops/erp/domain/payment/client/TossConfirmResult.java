package com.commerceops.erp.domain.payment.client;

import java.time.LocalDateTime;

public record TossConfirmResult(String status, String paymentKey, String orderId, Integer totalAmount,
                                String method, LocalDateTime approvedAt, String rawResponse) {}
