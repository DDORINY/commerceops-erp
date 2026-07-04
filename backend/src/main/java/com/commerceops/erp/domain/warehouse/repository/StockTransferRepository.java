package com.commerceops.erp.domain.warehouse.repository;

import com.commerceops.erp.domain.warehouse.entity.StockTransfer;
import com.commerceops.erp.domain.warehouse.enums.StockTransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    @Query(
        value = "SELECT t FROM StockTransfer t JOIN FETCH t.fromWarehouse JOIN FETCH t.toWarehouse JOIN FETCH t.product " +
                "WHERE (:status IS NULL OR t.status = :status)",
        countQuery = "SELECT COUNT(t) FROM StockTransfer t WHERE (:status IS NULL OR t.status = :status)"
    )
    Page<StockTransfer> findAllForAdmin(
            @Param("status") StockTransferStatus status,
            Pageable pageable
    );
}
