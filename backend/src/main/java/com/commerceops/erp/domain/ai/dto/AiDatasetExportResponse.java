package com.commerceops.erp.domain.ai.dto;

import com.commerceops.erp.domain.ai.enums.AiDatasetKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AiDatasetExportResponse(
        AiDatasetKey key,
        String label,
        LocalDateTime exportedAt,
        int rowCount,
        List<String> fields,
        List<Map<String, Object>> rows
) {
}
