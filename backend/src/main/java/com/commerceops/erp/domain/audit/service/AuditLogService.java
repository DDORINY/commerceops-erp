package com.commerceops.erp.domain.audit.service;

import com.commerceops.erp.domain.audit.dto.AuditLogResponse;
import com.commerceops.erp.domain.audit.dto.AuditLogSearchRequest;
import com.commerceops.erp.domain.audit.entity.AuditLog;
import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.repository.AuditLogRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    @Transactional
    public void record(User actor, AuditActionType actionType, String targetType, Long targetId,
                       String beforeStatus, String afterStatus, String summary) {
        record(actor, actionType, targetType, targetId, beforeStatus, afterStatus, summary, null, null, null);
    }

    @Transactional
    public void record(User actor, AuditActionType actionType, String targetType, Long targetId,
                       String beforeStatus, String afterStatus, String summary,
                       String beforeJson, String afterJson, String metadataJson) {
        HttpServletRequest request = requestProvider.getIfAvailable();
        auditLogRepository.save(AuditLog.builder()
                .actorId(actor.getId())
                .actorEmail(maskEmail(actor.getEmail()))
                .actorName(actor.getName())
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .beforeStatus(limit(beforeStatus, 50))
                .afterStatus(limit(afterStatus, 50))
                .summary(limit(summary, 500))
                .ipAddress(resolveIpAddress(request))
                .userAgent(limit(request != null ? request.getHeader("User-Agent") : null, 500))
                .requestMethod(request != null ? request.getMethod() : null)
                .requestPath(limit(request != null ? request.getRequestURI() : null, 500))
                .beforeJson(maskSensitiveJson(beforeJson))
                .afterJson(maskSensitiveJson(afterJson))
                .metadataJson(maskSensitiveJson(metadataJson))
                .build());
    }

    @Transactional
    public void recordPermissionDenied(User actor, String requiredPermissionCode) {
        if (actor == null) {
            return;
        }
        record(
                actor,
                AuditActionType.PERMISSION_DENIED,
                "PERMISSION",
                null,
                null,
                requiredPermissionCode,
                "권한이 없어 관리자 API 실행이 거부되었습니다.",
                null,
                null,
                "{\"requiredPermissionCode\":\"" + escapeJson(requiredPermissionCode) + "\"}"
        );
    }

    public PageResponse<AuditLogResponse> getAuditLogs(AuditLogSearchRequest search, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("createdAt").descending());
        return PageResponse.from(auditLogRepository.findAll(buildSpec(search), pageable).map(AuditLogResponse::from));
    }

    public AuditLogResponse getAuditLog(Long auditLogId) {
        return AuditLogResponse.from(auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND)));
    }

    private Specification<AuditLog> buildSpec(AuditLogSearchRequest search) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (search == null) {
                return predicates;
            }
            if (search.actorKeyword() != null && !search.actorKeyword().isBlank()) {
                String pattern = "%" + search.actorKeyword().trim().toLowerCase(Locale.ROOT) + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("actorEmail")), pattern),
                        cb.like(cb.lower(root.get("actorName")), pattern)
                ));
            }
            if (search.actionType() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("actionType"), search.actionType()));
            }
            if (search.targetType() != null && !search.targetType().isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("targetType"), search.targetType().trim().toUpperCase(Locale.ROOT)));
            }
            if (search.targetId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("targetId"), search.targetId()));
            }
            if (search.dateFrom() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), search.dateFrom()));
            }
            if (search.dateTo() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), search.dateTo()));
            }
            return predicates;
        };
    }

    private String resolveIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return limit(forwardedFor.split(",")[0].trim(), 100);
        }
        return limit(request.getRemoteAddr(), 100);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "-";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    private String maskSensitiveJson(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value
                .replaceAll("(?i)\"(password|accessToken|refreshToken|token)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"***\"")
                .replaceAll("(?i)\"(phone)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"***\"")
                .replaceAll("(?i)\"(email)\"\\s*:\\s*\"([^\"]{1,2})[^\"]*(@[^\"]+)\"", "\"$1\":\"$2***$3\"");
    }

    private String limit(String value, int length) {
        if (value == null || value.length() <= length) {
            return value;
        }
        return value.substring(0, length);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
