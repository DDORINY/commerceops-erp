package com.commerceops.erp.domain.accounting.repository;

import com.commerceops.erp.domain.accounting.entity.SettlementBatchItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementBatchItemRepository extends JpaRepository<SettlementBatchItem, Long> {

    List<SettlementBatchItem> findBySettlementBatchIdOrderByIdAsc(Long settlementBatchId);
}
