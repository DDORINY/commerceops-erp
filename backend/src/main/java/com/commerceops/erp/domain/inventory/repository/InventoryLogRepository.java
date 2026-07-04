package com.commerceops.erp.domain.inventory.repository;

import com.commerceops.erp.domain.inventory.entity.InventoryLog;
import com.commerceops.erp.domain.inventory.enums.InventoryLogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    @Query(
        value = "SELECT l FROM InventoryLog l JOIN FETCH l.product " +
                "WHERE (:productId IS NULL OR l.product.id = :productId) " +
                "AND (:type IS NULL OR l.type = :type)",
        countQuery = "SELECT COUNT(l) FROM InventoryLog l " +
                     "WHERE (:productId IS NULL OR l.product.id = :productId) " +
                     "AND (:type IS NULL OR l.type = :type)"
    )
    Page<InventoryLog> findAllForAdmin(
            @Param("productId") Long productId,
            @Param("type") InventoryLogType type,
            Pageable pageable
    );
}
