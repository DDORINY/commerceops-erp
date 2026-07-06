package com.commerceops.erp.domain.warehouse.repository;

import com.commerceops.erp.domain.warehouse.entity.WarehouseLocationStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WarehouseLocationStockRepository extends JpaRepository<WarehouseLocationStock, Long> {

    @Query(
            value = "SELECT s FROM WarehouseLocationStock s " +
                    "JOIN FETCH s.location JOIN FETCH s.warehouse JOIN FETCH s.sku JOIN FETCH s.product " +
                    "WHERE s.location.id = :locationId",
            countQuery = "SELECT COUNT(s) FROM WarehouseLocationStock s WHERE s.location.id = :locationId"
    )
    Page<WarehouseLocationStock> findByLocationIdForAdmin(@Param("locationId") Long locationId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM WarehouseLocationStock s " +
            "WHERE s.sku.id = :skuId AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId)")
    long sumQuantityBySkuAndWarehouse(@Param("skuId") Long skuId, @Param("warehouseId") Long warehouseId);
}
