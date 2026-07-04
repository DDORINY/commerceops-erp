package com.commerceops.erp.domain.ops.dto;

import java.util.Map;

public record OpsAnalyticsOverviewResponse(
        AccountingOverview accounting,
        SalesOverview sales,
        WarehouseOverview warehouse,
        String notes
) {
    public record AccountingOverview(
            long totalSales,
            long totalRefunds,
            long totalInboundAmount,
            long netSales,
            long entryCount
    ) {
    }

    public record SalesOverview(
            long totalOrders,
            long paidOrders,
            long cancelledOrders,
            long refundedOrders,
            long totalRevenue,
            long averagePaidOrderAmount,
            Map<String, Long> orderStatusCounts
    ) {
    }

    public record WarehouseOverview(
            long totalWarehouses,
            long activeWarehouses,
            long inactiveWarehouses,
            long totalStockQuantity,
            long totalReservedQuantity,
            long totalAvailableQuantity,
            Map<String, Long> reservationStatusCounts
    ) {
    }
}
