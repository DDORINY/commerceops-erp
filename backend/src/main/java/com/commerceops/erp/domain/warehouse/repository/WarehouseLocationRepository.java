package com.commerceops.erp.domain.warehouse.repository;

import com.commerceops.erp.domain.warehouse.entity.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, Long>, JpaSpecificationExecutor<WarehouseLocation> {
    boolean existsByWarehouseIdAndCodeIgnoreCase(Long warehouseId, String code);
    boolean existsByWarehouseIdAndCodeIgnoreCaseAndIdNot(Long warehouseId, String code, Long id);
}
