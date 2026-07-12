package com.commerceops.erp.domain.accounting.dto;

import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;

import java.time.LocalDateTime;

public record AccountingConsistencyIssueResponse(
        String issueType,
        String sourceType,
        Long sourceId,
        String sourceNumber,
        AccountingTransactionType expectedTransactionType,
        Long expectedAmount,
        String status,
        String message,
        LocalDateTime createdAt
) {
}
