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
        String ipAddress,
        String userAgent,
        String requestMethod,
        String requestPath,
        String beforeJson,
        String afterJson,
        String metadataJson,
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
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getRequestMethod(),
                auditLog.getRequestPath(),
                auditLog.getBeforeJson(),
                auditLog.getAfterJson(),
                auditLog.getMetadataJson(),
                auditLog.getCreatedAt()
        );
    }
}
