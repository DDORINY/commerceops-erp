package com.commerceops.erp.domain.inquiry.controller;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.inquiry.dto.InquiryAnswerRequest;
import com.commerceops.erp.domain.inquiry.dto.InquiryResponse;
import com.commerceops.erp.domain.inquiry.enums.InquiryStatus;
import com.commerceops.erp.domain.inquiry.service.InquiryService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryService inquiryService;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<PageResponse<InquiryResponse>> getInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.INQUIRY_REPLY);
        return ApiResponse.ok(inquiryService.getAdminInquiries(status, keyword, page, size));
    }

    @PatchMapping("/{id}/answer")
    public ApiResponse<InquiryResponse> answerInquiry(
            @PathVariable Long id,
            @Valid @RequestBody InquiryAnswerRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.INQUIRY_REPLY);
        InquiryResponse response = inquiryService.answerInquiry(id, request);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.INQUIRY_ANSWERED,
                "INQUIRY",
                id,
                null,
                response.status(),
                "문의에 답변했습니다: " + response.subject()
        );
        return ApiResponse.ok(response);
    }

    @PatchMapping("/{id}/close")
    public ApiResponse<InquiryResponse> closeInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INQUIRY_REPLY);
        InquiryResponse response = inquiryService.closeInquiry(id);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.INQUIRY_CLOSED,
                "INQUIRY",
                id,
                null,
                response.status(),
                "문의를 종료했습니다: " + response.subject()
        );
        return ApiResponse.ok(response);
    }
}
