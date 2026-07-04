package com.commerceops.erp.domain.returns.dto;

import com.commerceops.erp.domain.returns.entity.ReturnRequest;

import java.time.LocalDateTime;

public record ReturnResponse(
        Long returnId,
        Long orderId,
        String orderNumber,
        String userName,
        String reason,
        String reasonDetail,
        String status,
        String adminNote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReturnResponse from(ReturnRequest r) {
        return new ReturnResponse(
                r.getId(),
                r.getOrder().getId(),
                r.getOrder().getOrderNumber(),
                r.getUser().getName(),
                r.getReason().name(),
                r.getReasonDetail(),
                r.getStatus().name(),
                r.getAdminNote(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
