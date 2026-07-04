package com.commerceops.erp.domain.inquiry.controller;

import com.commerceops.erp.domain.inquiry.dto.InquiryAnswerRequest;
import com.commerceops.erp.domain.inquiry.dto.InquiryResponse;
import com.commerceops.erp.domain.inquiry.enums.InquiryStatus;
import com.commerceops.erp.domain.inquiry.service.InquiryService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryService inquiryService;

    @GetMapping
    public ApiResponse<PageResponse<InquiryResponse>> getInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return ApiResponse.ok(inquiryService.getAdminInquiries(status, keyword, page, size));
    }

    @PatchMapping("/{id}/answer")
    public ApiResponse<InquiryResponse> answerInquiry(
            @PathVariable Long id,
            @Valid @RequestBody InquiryAnswerRequest request
    ) {
        return ApiResponse.ok(inquiryService.answerInquiry(id, request));
    }

    @PatchMapping("/{id}/close")
    public ApiResponse<InquiryResponse> closeInquiry(@PathVariable Long id) {
        return ApiResponse.ok(inquiryService.closeInquiry(id));
    }
}
