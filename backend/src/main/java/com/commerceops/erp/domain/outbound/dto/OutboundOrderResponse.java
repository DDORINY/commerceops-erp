package com.commerceops.erp.domain.outbound.dto;

import com.commerceops.erp.domain.outbound.entity.OutboundOrder;

import java.time.LocalDateTime;
import java.util.List;

public record OutboundOrderResponse(
        Long id,
        String outboundNumber,
        Long orderId,
        String orderNumber,
        String customerName,
        String customerEmail,
        Long warehouseId,
        String warehouseName,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime pickedAt,
        LocalDateTime shippedAt,
        String memo,
        Integer totalQuantity,
        Integer pickedQuantity,
        Integer scannedQuantity,
        List<OutboundOrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OutboundOrderResponse from(OutboundOrder outboundOrder) {
        List<OutboundOrderItemResponse> itemResponses = outboundOrder.getItems().stream()
                .map(OutboundOrderItemResponse::from)
                .toList();
        return new OutboundOrderResponse(
                outboundOrder.getId(),
                outboundOrder.getOutboundNumber(),
                outboundOrder.getOrder().getId(),
                outboundOrder.getOrder().getOrderNumber(),
                outboundOrder.getOrder().getUser().getName(),
                outboundOrder.getOrder().getUser().getEmail(),
                outboundOrder.getWarehouse().getId(),
                outboundOrder.getWarehouse().getName(),
                outboundOrder.getStatus().name(),
                outboundOrder.getRequestedAt(),
                outboundOrder.getPickedAt(),
                outboundOrder.getShippedAt(),
                outboundOrder.getMemo(),
                outboundOrder.getItems().stream().mapToInt(item -> safe(item.getQuantity())).sum(),
                outboundOrder.getItems().stream().mapToInt(item -> safe(item.getPickedQuantity())).sum(),
                outboundOrder.getItems().stream().mapToInt(item -> safe(item.getScannedQuantity())).sum(),
                itemResponses,
                outboundOrder.getCreatedAt(),
                outboundOrder.getUpdatedAt()
        );
    }

    private static int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
