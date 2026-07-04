package com.commerceops.erp.domain.inquiry.dto;

import com.commerceops.erp.domain.inquiry.entity.Inquiry;

import java.time.LocalDateTime;

public record InquiryResponse(
        Long inquiryId,
        String userName,
        Long productId,
        String productName,
        String type,
        String subject,
        String content,
        String answer,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InquiryResponse from(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getUser().getName(),
                inquiry.getProduct() != null ? inquiry.getProduct().getId() : null,
                inquiry.getProduct() != null ? inquiry.getProduct().getName() : null,
                inquiry.getType().name(),
                inquiry.getSubject(),
                inquiry.getContent(),
                inquiry.getAnswer(),
                inquiry.getStatus().name(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt()
        );
    }
}
