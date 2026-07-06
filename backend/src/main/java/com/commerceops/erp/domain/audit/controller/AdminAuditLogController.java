package com.commerceops.erp.domain.audit.controller;

import com.commerceops.erp.domain.audit.dto.AuditLogResponse;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.AUDIT_LOG_READ);
        return ResponseEntity.ok(ApiResponse.ok(
                "관리자 작업 이력 조회가 완료되었습니다.",
                auditLogService.getAuditLogs(targetType, page, size)
        ));
    }
}
