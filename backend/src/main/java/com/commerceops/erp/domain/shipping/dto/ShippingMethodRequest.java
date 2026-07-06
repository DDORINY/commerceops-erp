package com.commerceops.erp.domain.shipping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShippingMethodRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 100) String name,
        Long carrierId,
        @NotNull @Min(0) Integer defaultFee,
        @Size(max = 500) String description,
        Boolean active
) {
}
