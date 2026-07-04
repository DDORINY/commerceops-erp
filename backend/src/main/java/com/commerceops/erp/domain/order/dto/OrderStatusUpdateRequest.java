package com.commerceops.erp.domain.order.dto;

import com.commerceops.erp.domain.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull OrderStatus status
) {}
