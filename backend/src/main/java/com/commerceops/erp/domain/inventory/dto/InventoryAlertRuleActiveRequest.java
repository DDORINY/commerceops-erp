package com.commerceops.erp.domain.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record InventoryAlertRuleActiveRequest(
        @NotNull Boolean active
) {
}
