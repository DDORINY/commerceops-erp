package com.commerceops.erp.domain.payment.client;

import lombok.Getter;

@Getter
public class TossPaymentClientException extends RuntimeException {
    private final String code;
    private final int httpStatus;
    private final boolean retryable;

    public TossPaymentClientException(String code, String message, int httpStatus, boolean retryable) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }
}
