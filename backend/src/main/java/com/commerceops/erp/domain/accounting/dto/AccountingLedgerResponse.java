package com.commerceops.erp.domain.accounting.dto;

import com.commerceops.erp.domain.accounting.entity.AccountingLedger;

import java.time.LocalDateTime;

public record AccountingLedgerResponse(
        Long ledgerId,
        String ledgerNumber,
        String period,
        String status,
        LocalDateTime closedAt,
        Long closedById,
        String closedByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AccountingLedgerResponse from(AccountingLedger ledger) {
        return new AccountingLedgerResponse(
                ledger.getId(),
                ledger.getLedgerNumber(),
                ledger.getPeriod(),
                ledger.getStatus().name(),
                ledger.getClosedAt(),
                ledger.getClosedBy() != null ? ledger.getClosedBy().getId() : null,
                ledger.getClosedBy() != null ? ledger.getClosedBy().getName() : null,
                ledger.getCreatedAt(),
                ledger.getUpdatedAt()
        );
    }
}
