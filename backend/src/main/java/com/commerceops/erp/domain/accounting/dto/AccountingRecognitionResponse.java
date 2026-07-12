package com.commerceops.erp.domain.accounting.dto;

import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;
import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;

import java.time.LocalDateTime;

public record AccountingRecognitionResponse(
        AccountingTransactionType transactionType,
        AccountingReferenceType referenceType,
        Long referenceId,
        Long sourceId,
        String sourceNumber,
        Long amount,
        boolean recognized,
        Long transactionId,
        String transactionNumber,
        LocalDateTime occurredAt,
        String message
) {
    public static AccountingRecognitionResponse from(
            AccountingTransactionType transactionType,
            AccountingReferenceType referenceType,
            Long referenceId,
            Long sourceId,
            String sourceNumber,
            Long amount,
            AccountingTransaction transaction,
            String message
    ) {
        return new AccountingRecognitionResponse(
                transactionType,
                referenceType,
                referenceId,
                sourceId,
                sourceNumber,
                amount,
                transaction != null,
                transaction != null ? transaction.getId() : null,
                transaction != null ? transaction.getTransactionNumber() : null,
                transaction != null ? transaction.getOccurredAt() : null,
                message
        );
    }
}
