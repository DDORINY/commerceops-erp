package com.commerceops.erp.domain.hr.dto;

import com.commerceops.erp.domain.hr.entity.Position;

import java.time.LocalDateTime;

public record PositionResponse(
        Long id,
        String name,
        Integer level,
        Integer sortOrder,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PositionResponse from(Position position) {
        return new PositionResponse(
                position.getId(),
                position.getName(),
                position.getLevel(),
                position.getSortOrder(),
                position.getActive(),
                position.getCreatedAt(),
                position.getUpdatedAt()
        );
    }
}
