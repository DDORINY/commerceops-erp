package com.commerceops.erp.domain.accounting.dto;

import com.commerceops.erp.domain.accounting.entity.ShippingCostEntry;

import java.time.LocalDateTime;

public record ShippingCostEntryResponse(
        Long id,
        Long shipmentId,
        Long orderId,
        String orderNumber,
        Long carrierId,
        String carrierName,
        Long shippingMethodId,
        String shippingMethodName,
        Long costAmount,
        Long chargedAmount,
        Long marginAmount,
        String settlementStatus,
        LocalDateTime occurredAt,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ShippingCostEntryResponse from(ShippingCostEntry entry) {
        return new ShippingCostEntryResponse(
                entry.getId(),
                entry.getShipment().getId(),
                entry.getShipment().getOrder().getId(),
                entry.getShipment().getOrder().getOrderNumber(),
                entry.getCarrier() != null ? entry.getCarrier().getId() : null,
                entry.getCarrier() != null ? entry.getCarrier().getName() : entry.getShipment().getCarrier(),
                entry.getShippingMethod() != null ? entry.getShippingMethod().getId() : null,
                entry.getShippingMethod() != null ? entry.getShippingMethod().getName() : null,
                entry.getCostAmount(),
                entry.getChargedAmount(),
                entry.getChargedAmount() - entry.getCostAmount(),
                entry.getSettlementStatus().name(),
                entry.getOccurredAt(),
                entry.getMemo(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
