package com.commerceops.erp.domain.audit.dto;

import com.commerceops.erp.domain.audit.enums.AuditActionType;

import java.time.LocalDateTime;

public record AuditLogSearchRequest(
        String actorKeyword,
        AuditActionType actionType,
        String targetType,
        Long targetId,
        LocalDateTime dateFrom,
        LocalDateTime dateTo
) {
}
