package com.commerceops.erp.domain.returns.dto;

import com.commerceops.erp.domain.returns.entity.ReturnShipmentInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReturnShipmentInfoResponse(
        Long id,
        Long returnId,
        String carrier,
        String trackingNumber,
        String status,
        BigDecimal shippingFee,
        String feePayer,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReturnShipmentInfoResponse from(ReturnShipmentInfo info) {
        return new ReturnShipmentInfoResponse(
                info.getId(),
                info.getReturnRequest().getId(),
                info.getCarrier(),
                info.getTrackingNumber(),
                info.getStatus().name(),
                info.getShippingFee(),
                info.getFeePayer().name(),
                info.getMemo(),
                info.getCreatedAt(),
                info.getUpdatedAt()
        );
    }
}
