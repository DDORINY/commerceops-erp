package com.commerceops.erp.domain.dashboard.dto;

public record SalesResponse(
        String date,
        Long salesAmount,
        Long orderCount
) {}
