package com.commerceops.erp.domain.accounting.dto;

import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;

import java.time.LocalDateTime;

public record AccountingTransactionResponse(
        Long transactionId,
        Long ledgerId,
        String ledgerNumber,
        String transactionNumber,
        String type,
        String direction,
        Long amount,
        String referenceType,
        Long referenceId,
        LocalDateTime occurredAt,
        String memo,
        Long createdById,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AccountingTransactionResponse from(AccountingTransaction transaction) {
        return new AccountingTransactionResponse(
                transaction.getId(),
                transaction.getLedger() != null ? transaction.getLedger().getId() : null,
                transaction.getLedger() != null ? transaction.getLedger().getLedgerNumber() : null,
                transaction.getTransactionNumber(),
                transaction.getType().name(),
                transaction.getDirection().name(),
                transaction.getAmount(),
                transaction.getReferenceType().name(),
                transaction.getReferenceId(),
                transaction.getOccurredAt(),
                transaction.getMemo(),
                transaction.getCreatedBy() != null ? transaction.getCreatedBy().getId() : null,
                transaction.getCreatedBy() != null ? transaction.getCreatedBy().getName() : null,
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
