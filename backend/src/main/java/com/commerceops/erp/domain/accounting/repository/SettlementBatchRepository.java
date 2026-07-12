package com.commerceops.erp.domain.accounting.repository;

import com.commerceops.erp.domain.accounting.entity.SettlementBatch;
import com.commerceops.erp.domain.accounting.enums.SettlementBatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long> {

    boolean existsByPeriodStartAndPeriodEndAndStatusNot(LocalDate periodStart, LocalDate periodEnd, SettlementBatchStatus status);

    Optional<SettlementBatch> findById(Long id);

    @Query("SELECT b FROM SettlementBatch b WHERE (:status IS NULL OR b.status = :status)")
    Page<SettlementBatch> findAllForAdmin(@Param("status") SettlementBatchStatus status, Pageable pageable);
}
