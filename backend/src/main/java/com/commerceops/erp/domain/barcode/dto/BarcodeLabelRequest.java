package com.commerceops.erp.domain.barcode.dto;

import jakarta.validation.constraints.Size;

public record BarcodeLabelRequest(
        @Size(max = 50) String labelFormat
) {
}
