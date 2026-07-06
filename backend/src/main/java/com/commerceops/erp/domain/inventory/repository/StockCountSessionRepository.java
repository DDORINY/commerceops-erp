package com.commerceops.erp.domain.inventory.repository;

import com.commerceops.erp.domain.inventory.entity.StockCountSession;
import com.commerceops.erp.domain.inventory.enums.StockCountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockCountSessionRepository extends JpaRepository<StockCountSession, Long> {

    boolean existsByCountNumber(String countNumber);

    @EntityGraph(attributePaths = {"warehouse"})
    Page<StockCountSession> findByStatus(StockCountStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"warehouse"})
    Page<StockCountSession> findAll(Pageable pageable);

    @Query("SELECT s FROM StockCountSession s JOIN FETCH s.warehouse LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.sku LEFT JOIN FETCH i.product WHERE s.id = :id")
    Optional<StockCountSession> findDetailById(@Param("id") Long id);
}
