package com.commerceops.erp.domain.barcode.repository;

import com.commerceops.erp.domain.barcode.entity.BarcodeLabel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BarcodeLabelRepository extends JpaRepository<BarcodeLabel, Long>, JpaSpecificationExecutor<BarcodeLabel> {

    @EntityGraph(attributePaths = {"sku", "sku.product"})
    Page<BarcodeLabel> findAll(Pageable pageable);
}
