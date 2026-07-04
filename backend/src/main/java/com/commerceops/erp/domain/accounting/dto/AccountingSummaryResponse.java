package com.commerceops.erp.domain.accounting.dto;

public record AccountingSummaryResponse(
        long totalSales,
        long totalRefunds,
        long totalInbound,
        long netSales
) {}
