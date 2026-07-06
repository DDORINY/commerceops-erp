package com.commerceops.erp.domain.inventory.repository;

import com.commerceops.erp.domain.inventory.entity.StockCountItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockCountItemRepository extends JpaRepository<StockCountItem, Long> {

    Optional<StockCountItem> findBySessionIdAndSkuId(Long sessionId, Long skuId);
}
