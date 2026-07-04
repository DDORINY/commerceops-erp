package com.commerceops.erp.domain.audit.dto;

import com.commerceops.erp.domain.audit.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long actorId,
        String actorEmail,
        String actorName,
        String actionType,
        String targetType,
        Long targetId,
        String beforeStatus,
        String afterStatus,
        String summary,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorId(),
                auditLog.getActorEmail(),
                auditLog.getActorName(),
                auditLog.getActionType().name(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getBeforeStatus(),
                auditLog.getAfterStatus(),
                auditLog.getSummary(),
                auditLog.getCreatedAt()
        );
    }
}
