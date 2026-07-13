package com.commerceops.erp.domain.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AiReportResponse(
        String id,
        String title,
        String summary,
        String relatedModule,
        String modelName,
        List<String> evidenceSources,
        List<String> interpretationGuide,
        LocalDateTime generatedAt
) {
}
