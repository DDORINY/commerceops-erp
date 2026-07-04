package com.commerceops.erp.domain.inquiry.dto;

import com.commerceops.erp.domain.inquiry.enums.InquiryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InquiryCreateRequest(
        @NotNull InquiryType type,
        @NotBlank String subject,
        @NotBlank String content
) {}
