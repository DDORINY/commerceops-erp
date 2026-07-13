package com.commerceops.erp.domain.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AiOperationsHealthResponse(
        boolean available,
        String status,
        String message,
        List<String> checkedItems,
        LocalDateTime checkedAt
) {
}
