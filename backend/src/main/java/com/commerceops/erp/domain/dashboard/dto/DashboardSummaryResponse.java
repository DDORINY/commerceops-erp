package com.commerceops.erp.domain.dashboard.dto;

import java.util.Map;

public record DashboardSummaryResponse(
        Long totalSales,
        Long todaySales,
        Long totalOrders,
        Long todayOrders,
        Long totalCustomers,
        Long totalProducts,
        Long soldOutProductCount,
        Long lowStockProductCount,
        Long pendingOrderCount,
        Map<String, Long> orderStatusCounts
) {}
