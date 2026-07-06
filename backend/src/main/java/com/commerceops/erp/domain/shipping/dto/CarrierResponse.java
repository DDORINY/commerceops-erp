package com.commerceops.erp.domain.shipping.dto;

import com.commerceops.erp.domain.shipping.entity.Carrier;

import java.time.LocalDateTime;

public record CarrierResponse(
        Long id,
        String code,
        String name,
        String trackingUrlTemplate,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CarrierResponse from(Carrier carrier) {
        return new CarrierResponse(
                carrier.getId(),
                carrier.getCode(),
                carrier.getName(),
                carrier.getTrackingUrlTemplate(),
                carrier.getActive(),
                carrier.getCreatedAt(),
                carrier.getUpdatedAt()
        );
    }
}
