package com.commerceops.erp.domain.accounting.dto;

import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;
import com.commerceops.erp.domain.order.entity.Order;

import java.time.LocalDateTime;

public record OrderRevenueRecognitionResponse(
        Long orderId,
        String orderNumber,
        String orderStatus,
        String paymentStatus,
        Integer orderAmount,
        boolean recognized,
        Long transactionId,
        String transactionNumber,
        Long recognizedAmount,
        LocalDateTime recognizedAt,
        String message
) {
    public static OrderRevenueRecognitionResponse from(Order order, AccountingTransaction transaction, String message) {
        return new OrderRevenueRecognitionResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                order.getTotalPrice(),
                transaction != null,
                transaction != null ? transaction.getId() : null,
                transaction != null ? transaction.getTransactionNumber() : null,
                transaction != null ? transaction.getAmount() : null,
                transaction != null ? transaction.getOccurredAt() : null,
                message
        );
    }
}
