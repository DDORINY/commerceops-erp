package com.commerceops.erp.domain.production.repository;

import com.commerceops.erp.domain.production.entity.ProductionOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionOrderItemRepository extends JpaRepository<ProductionOrderItem, Long> {
}
