package com.commerceops.erp.domain.production.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductionOrderCompleteRequest(
        @NotEmpty List<@Valid ProductionOrderCompleteItemRequest> items,
        @Size(max = 1000) String memo
) {
}
