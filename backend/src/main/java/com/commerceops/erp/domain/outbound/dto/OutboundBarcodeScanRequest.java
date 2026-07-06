package com.commerceops.erp.domain.outbound.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OutboundBarcodeScanRequest(
        @NotBlank @Size(max = 100) String barcode,
        @Min(1) Integer quantity
) {
}
