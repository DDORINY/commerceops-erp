package com.commerceops.erp.domain.production.repository;

import com.commerceops.erp.domain.production.entity.ProductionOrder;
import com.commerceops.erp.domain.production.enums.ProductionOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long>, JpaSpecificationExecutor<ProductionOrder> {

    boolean existsByProductionNumber(String productionNumber);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = {"warehouse", "items", "items.sku", "items.product"})
    Optional<ProductionOrder> findWithItemsById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM ProductionOrder o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.sku LEFT JOIN FETCH i.product JOIN FETCH o.warehouse WHERE o.id = :id")
    Optional<ProductionOrder> findForUpdate(@Param("id") Long id);

    long countByStatus(ProductionOrderStatus status);
}
