package com.commerceops.erp.domain.accounting.dto;

import com.commerceops.erp.domain.accounting.entity.AccountingEntry;

import java.time.LocalDateTime;

public record AccountingEntryResponse(
        Long entryId,
        String type,
        Integer amount,
        String description,
        String referenceId,
        LocalDateTime createdAt
) {
    public static AccountingEntryResponse from(AccountingEntry e) {
        return new AccountingEntryResponse(
                e.getId(),
                e.getType().name(),
                e.getAmount(),
                e.getDescription(),
                e.getReferenceId(),
                e.getCreatedAt()
        );
    }
}
