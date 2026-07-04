package com.commerceops.erp.domain.dashboard.dto;

public record TopProductResponse(
        Long productId,
        String productName,
        Long orderCount,
        Long salesAmount
) {}
