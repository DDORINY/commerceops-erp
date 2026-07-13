package com.commerceops.erp.domain.ai.dto;

import com.commerceops.erp.domain.ai.enums.AiRiskLevel;

import java.time.LocalDateTime;
import java.util.Map;

public record AiInsightResponse(
        String id,
        String targetType,
        Long targetId,
        String title,
        double score,
        AiRiskLevel riskLevel,
        String reason,
        Map<String, Object> features,
        String modelName,
        LocalDateTime generatedAt
) {
}
