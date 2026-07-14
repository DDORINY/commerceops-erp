package com.commerceops.erp.domain.payment.client;

public interface TossPaymentClient {
    TossConfirmResult confirm(String paymentKey, String orderId, Integer amount, String idempotencyKey);
}
