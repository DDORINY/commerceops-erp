package com.commerceops.erp.domain.audit.service;

import com.commerceops.erp.domain.audit.dto.AuditLogResponse;
import com.commerceops.erp.domain.audit.entity.AuditLog;
import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.repository.AuditLogRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(User actor, AuditActionType actionType, String targetType, Long targetId,
                       String beforeStatus, String afterStatus, String summary) {
        auditLogRepository.save(AuditLog.builder()
                .actorId(actor.getId())
                .actorEmail(actor.getEmail())
                .actorName(actor.getName())
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .summary(summary)
                .build());
    }

    public PageResponse<AuditLogResponse> getAuditLogs(String targetType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (targetType == null || targetType.isBlank()) {
            return PageResponse.from(auditLogRepository.findAll(pageable).map(AuditLogResponse::from));
        }
        return PageResponse.from(
                auditLogRepository.findByTargetTypeOrderByCreatedAtDesc(targetType, pageable)
                        .map(AuditLogResponse::from)
        );
    }
}
