package com.commerceops.erp.domain.accounting.dto;

import com.commerceops.erp.domain.accounting.entity.SettlementBatch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SettlementBatchResponse(
        Long id,
        String batchNumber,
        LocalDate periodStart,
        LocalDate periodEnd,
        String status,
        Long totalSales,
        Long totalRefunds,
        Long totalShippingFee,
        Long totalShippingCost,
        Long netAmount,
        LocalDateTime closedAt,
        Long closedById,
        String closedByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SettlementBatchItemResponse> items
) {
    public static SettlementBatchResponse from(SettlementBatch batch) {
        return from(batch, List.of());
    }

    public static SettlementBatchResponse from(SettlementBatch batch, List<SettlementBatchItemResponse> items) {
        return new SettlementBatchResponse(
                batch.getId(),
                batch.getBatchNumber(),
                batch.getPeriodStart(),
                batch.getPeriodEnd(),
                batch.getStatus().name(),
                batch.getTotalSales(),
                batch.getTotalRefunds(),
                batch.getTotalShippingFee(),
                batch.getTotalShippingCost(),
                batch.getTotalSales() + batch.getTotalShippingFee() - batch.getTotalRefunds() - batch.getTotalShippingCost(),
                batch.getClosedAt(),
                batch.getClosedBy() != null ? batch.getClosedBy().getId() : null,
                batch.getClosedBy() != null ? batch.getClosedBy().getName() : null,
                batch.getCreatedAt(),
                batch.getUpdatedAt(),
                items
        );
    }
}
