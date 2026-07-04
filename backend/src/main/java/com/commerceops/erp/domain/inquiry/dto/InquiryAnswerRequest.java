package com.commerceops.erp.domain.inquiry.dto;

import jakarta.validation.constraints.NotBlank;

public record InquiryAnswerRequest(@NotBlank String answer) {}
