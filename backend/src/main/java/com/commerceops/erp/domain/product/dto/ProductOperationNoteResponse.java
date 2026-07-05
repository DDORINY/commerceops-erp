package com.commerceops.erp.domain.product.dto;

import com.commerceops.erp.domain.product.entity.ProductOperationNote;

import java.time.LocalDateTime;

public record ProductOperationNoteResponse(
        Long id,
        Long productId,
        Long writerUserId,
        String writerEmail,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductOperationNoteResponse from(ProductOperationNote note) {
        return new ProductOperationNoteResponse(
                note.getId(),
                note.getProduct().getId(),
                note.getWriterUserId(),
                note.getWriterEmail(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
