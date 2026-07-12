package com.commerceops.erp.domain.accounting.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SettlementBatchCreateRequest(
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd
) {
}
