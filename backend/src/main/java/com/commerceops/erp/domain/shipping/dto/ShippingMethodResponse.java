package com.commerceops.erp.domain.shipping.dto;

import com.commerceops.erp.domain.shipping.entity.ShippingMethod;

import java.time.LocalDateTime;

public record ShippingMethodResponse(
        Long id,
        String code,
        String name,
        Long carrierId,
        String carrierName,
        Integer defaultFee,
        String description,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ShippingMethodResponse from(ShippingMethod method) {
        return new ShippingMethodResponse(
                method.getId(),
                method.getCode(),
                method.getName(),
                method.getCarrier() != null ? method.getCarrier().getId() : null,
                method.getCarrier() != null ? method.getCarrier().getName() : null,
                method.getDefaultFee(),
                method.getDescription(),
                method.getActive(),
                method.getCreatedAt(),
                method.getUpdatedAt()
        );
    }
}
