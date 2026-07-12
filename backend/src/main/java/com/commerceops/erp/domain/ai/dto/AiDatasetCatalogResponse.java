package com.commerceops.erp.domain.ai.dto;

import com.commerceops.erp.domain.ai.enums.AiDatasetKey;

import java.util.List;

public record AiDatasetCatalogResponse(
        AiDatasetKey key,
        String label,
        String description,
        List<String> fields
) {
}
