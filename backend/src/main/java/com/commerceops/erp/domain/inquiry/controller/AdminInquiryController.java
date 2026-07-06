package com.commerceops.erp.domain.inquiry.controller;

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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryService inquiryService;
    private final PermissionChecker permissionChecker;

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
        return ApiResponse.ok(inquiryService.answerInquiry(id, request));
    }

    @PatchMapping("/{id}/close")
    public ApiResponse<InquiryResponse> closeInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INQUIRY_REPLY);
        return ApiResponse.ok(inquiryService.closeInquiry(id));
    }
}
