package com.commerceops.erp.domain.inquiry.controller;

import com.commerceops.erp.domain.inquiry.dto.InquiryCreateRequest;
import com.commerceops.erp.domain.inquiry.dto.InquiryResponse;
import com.commerceops.erp.domain.inquiry.service.InquiryService;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping("/api/products/{productId}/inquiries")
    public ApiResponse<InquiryResponse> createProductInquiry(
            @PathVariable Long productId,
            @Valid @RequestBody InquiryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(inquiryService.createInquiry(productId, userDetails.getUser(), request));
    }

    @PostMapping("/api/inquiries")
    public ApiResponse<InquiryResponse> createInquiry(
            @Valid @RequestBody InquiryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(inquiryService.createInquiry(null, userDetails.getUser(), request));
    }

    @GetMapping("/api/my/inquiries")
    public ApiResponse<List<InquiryResponse>> getMyInquiries(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(inquiryService.getMyInquiries(userDetails.getUser()));
    }

    @GetMapping("/api/products/{productId}/inquiries")
    public ApiResponse<List<InquiryResponse>> getProductInquiries(@PathVariable Long productId) {
        return ApiResponse.ok(inquiryService.getProductInquiries(productId));
    }
}
