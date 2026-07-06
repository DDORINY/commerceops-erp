package com.commerceops.erp.domain.production.repository;

import com.commerceops.erp.domain.production.entity.ProductionReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductionReceiptRepository extends JpaRepository<ProductionReceipt, Long>, JpaSpecificationExecutor<ProductionReceipt> {

    @EntityGraph(attributePaths = {"productionOrder", "sku", "product", "warehouse"})
    Optional<ProductionReceipt> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"productionOrder", "sku", "product", "warehouse"})
    Page<ProductionReceipt> findAll(Pageable pageable);
}
