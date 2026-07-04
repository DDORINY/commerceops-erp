package com.commerceops.erp.domain.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InventoryStockChangeResponse(
        Long warehouseId,
        Long productId,
        Integer quantity,
        Integer beforeStock,
        Integer afterStock,
        Integer beforeWarehouseStock,
        Integer afterWarehouseStock,
        String type
) {}
