package com.commerceops.erp.domain.production.dto;

import jakarta.validation.constraints.Size;

public record ProductionOrderCancelRequest(
        @Size(max = 1000) String memo
) {
}
