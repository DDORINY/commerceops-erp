package com.commerceops.erp.domain.warehouse.dto;

import com.commerceops.erp.domain.warehouse.entity.StockTransfer;

import java.time.LocalDateTime;

public record StockTransferResponse(
        Long transferId,
        String transferNumber,
        Long fromWarehouseId,
        String fromWarehouseName,
        Long toWarehouseId,
        String toWarehouseName,
        Long productId,
        String productName,
        int quantity,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime completedAt
) {
    public static StockTransferResponse from(StockTransfer transfer) {
        return new StockTransferResponse(
                transfer.getId(), transfer.getTransferNumber(),
                transfer.getFromWarehouse().getId(), transfer.getFromWarehouse().getName(),
                transfer.getToWarehouse().getId(), transfer.getToWarehouse().getName(),
                transfer.getProduct().getId(), transfer.getProduct().getName(), transfer.getQuantity(),
                transfer.getStatus().name(), transfer.getRequestedAt(), transfer.getCompletedAt()
        );
    }
}
