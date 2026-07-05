package com.commerceops.erp.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductOperationNoteRequest(
        @NotBlank @Size(max = 2000) String content
) {
}
