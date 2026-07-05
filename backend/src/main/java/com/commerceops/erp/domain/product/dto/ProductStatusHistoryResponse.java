package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.entity.ProductStatusHistory;

import java.time.LocalDateTime;

public record ProductStatusHistoryResponse(
        Long id,
        Long productId,
        Long changedByUserId,
        String changedByEmail,
        String previousSalesStatus,
        String newSalesStatus,
        String previousDisplayStatus,
        String newDisplayStatus,
        String reason,
        LocalDateTime createdAt
) {
    public static ProductStatusHistoryResponse from(ProductStatusHistory history) {
        return new ProductStatusHistoryResponse(
                history.getId(),
                history.getProduct().getId(),
                history.getChangedByUserId(),
                history.getChangedByEmail(),
                history.getPreviousSalesStatus() != null ? history.getPreviousSalesStatus().name() : null,
                history.getNewSalesStatus() != null ? history.getNewSalesStatus().name() : null,
                history.getPreviousDisplayStatus() != null ? history.getPreviousDisplayStatus().name() : null,
                history.getNewDisplayStatus() != null ? history.getNewDisplayStatus().name() : null,
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
