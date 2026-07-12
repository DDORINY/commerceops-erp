package com.commerceops.erp.domain.accounting.dto;

import java.util.List;

public record AccountingConsistencyReportResponse(
        long missingRevenueCount,
        long missingRefundCount,
        long missingReturnRefundCount,
        long missingReturnFeeCount,
        long missingShippingCostCount,
        List<AccountingConsistencyIssueResponse> issues
) {
    public long totalIssueCount() {
        return missingRevenueCount
                + missingRefundCount
                + missingReturnRefundCount
                + missingReturnFeeCount
                + missingShippingCostCount;
    }
}
