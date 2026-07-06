package com.commerceops.erp.domain.warehouse.repository;

import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, Long> {

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM WarehouseStock s")
    long sumQuantity();

    @Query("SELECT COALESCE(SUM(s.reservedQuantity), 0) FROM WarehouseStock s")
    long sumReservedQuantity();

    @Query("SELECT COALESCE(SUM(s.quantity - s.reservedQuantity), 0) FROM WarehouseStock s")
    long sumAvailableQuantity();

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM WarehouseStock s WHERE s.product.id = :productId")
    long sumQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(s.quantity - s.reservedQuantity), 0) FROM WarehouseStock s WHERE s.product.id = :productId")
    long sumAvailableQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT s FROM WarehouseStock s JOIN FETCH s.warehouse WHERE s.product.id = :productId ORDER BY s.warehouse.name ASC")
    List<WarehouseStock> findByProductIdWithWarehouse(@Param("productId") Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM WarehouseStock s WHERE s.warehouse.id = :warehouseId AND s.product.id = :productId")
    Optional<WarehouseStock> findForUpdate(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM WarehouseStock s JOIN FETCH s.warehouse " +
            "WHERE s.product.id = :productId AND (s.quantity - s.reservedQuantity) > 0 " +
            "AND s.warehouse.active = true ORDER BY s.id")
    List<WarehouseStock> findAvailableForUpdate(@Param("productId") Long productId);

    @Query(
        value = "SELECT s FROM WarehouseStock s JOIN FETCH s.warehouse JOIN FETCH s.product " +
                "WHERE (:warehouseId IS NULL OR s.warehouse.id = :warehouseId) " +
                "AND (:keyword IS NULL OR LOWER(s.product.name) LIKE LOWER(CONCAT('%', :keyword, '%')))",
        countQuery = "SELECT COUNT(s) FROM WarehouseStock s " +
                "WHERE (:warehouseId IS NULL OR s.warehouse.id = :warehouseId) " +
                "AND (:keyword IS NULL OR LOWER(s.product.name) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<WarehouseStock> findAllForAdmin(
            @Param("warehouseId") Long warehouseId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
