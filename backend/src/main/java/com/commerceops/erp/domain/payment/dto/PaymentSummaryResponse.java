package com.commerceops.erp.domain.payment.dto;

import com.commerceops.erp.domain.payment.entity.Payment;
import java.time.LocalDateTime;

public record PaymentSummaryResponse(String provider, String status, String method, Integer amount,
                                     LocalDateTime approvedAt, String failureCode, String failureMessage) {
    public static PaymentSummaryResponse from(Payment payment) {
        return new PaymentSummaryResponse(payment.getProvider(), payment.getPaymentStatus().name(),
                payment.getPaymentMethod() == null ? null : payment.getPaymentMethod().name(),
                payment.getApprovedAmount(), payment.getApprovedAt(), payment.getFailureCode(), payment.getFailureMessage());
    }
}
