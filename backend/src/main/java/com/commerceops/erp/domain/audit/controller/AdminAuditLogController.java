package com.commerceops.erp.domain.audit.controller;

import com.commerceops.erp.domain.audit.dto.AuditLogResponse;
import com.commerceops.erp.domain.audit.dto.AuditLogSearchRequest;
import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String actorKeyword,
            @RequestParam(required = false) AuditActionType actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.AUDIT_LOG_READ);
        return ResponseEntity.ok(ApiResponse.ok(
                "관리자 작업 이력 조회가 완료되었습니다.",
                auditLogService.getAuditLogs(
                        new AuditLogSearchRequest(actorKeyword, actionType, targetType, targetId, dateFrom, dateTo),
                        page,
                        size
                )
        ));
    }

    @GetMapping("/{auditLogId}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLog(
            @PathVariable Long auditLogId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.AUDIT_LOG_READ);
        return ResponseEntity.ok(ApiResponse.ok(
                "관리자 작업 이력 상세 조회가 완료되었습니다.",
                auditLogService.getAuditLog(auditLogId)
        ));
    }
}
