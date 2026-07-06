package com.commerceops.erp.domain.inventory.repository;

import com.commerceops.erp.domain.inventory.entity.InventoryAlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InventoryAlertRuleRepository extends JpaRepository<InventoryAlertRule, Long>, JpaSpecificationExecutor<InventoryAlertRule> {
    boolean existsBySkuIdAndWarehouseId(Long skuId, Long warehouseId);
    boolean existsBySkuIdAndWarehouseIsNull(Long skuId);
    boolean existsBySkuIdAndWarehouseIdAndIdNot(Long skuId, Long warehouseId, Long id);
    boolean existsBySkuIdAndWarehouseIsNullAndIdNot(Long skuId, Long id);
}
