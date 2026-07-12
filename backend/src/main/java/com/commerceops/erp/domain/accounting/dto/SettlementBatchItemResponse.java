package com.commerceops.erp.domain.accounting.dto;

import com.commerceops.erp.domain.accounting.entity.SettlementBatchItem;

import java.time.LocalDateTime;

public record SettlementBatchItemResponse(
        Long id,
        String referenceType,
        Long referenceId,
        String itemType,
        Long amount,
        String memo,
        String status,
        LocalDateTime createdAt
) {
    public static SettlementBatchItemResponse from(SettlementBatchItem item) {
        return new SettlementBatchItemResponse(
                item.getId(),
                item.getReferenceType().name(),
                item.getReferenceId(),
                item.getItemType().name(),
                item.getAmount(),
                item.getMemo(),
                item.getStatus().name(),
                item.getCreatedAt()
        );
    }
}
