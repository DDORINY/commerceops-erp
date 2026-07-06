package com.commerceops.erp.domain.outbound.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OutboundOrderCreateRequest(
        @NotNull Long orderId,
        @NotNull Long warehouseId,
        @Size(max = 500) String memo
) {
}
