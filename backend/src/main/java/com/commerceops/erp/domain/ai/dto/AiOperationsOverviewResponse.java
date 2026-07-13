package com.commerceops.erp.domain.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AiOperationsOverviewResponse(
        String datasetStatus,
        String modelStatus,
        List<String> enabledModules,
        List<AiInsightResponse> highlights,
        LocalDateTime generatedAt
) {
}
