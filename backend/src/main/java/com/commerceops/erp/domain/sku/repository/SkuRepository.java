package com.commerceops.erp.domain.sku.repository;

import com.commerceops.erp.domain.sku.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SkuRepository extends JpaRepository<Sku, Long>, JpaSpecificationExecutor<Sku> {

    boolean existsBySkuCode(String skuCode);

    boolean existsBySkuCodeAndIdNot(String skuCode, Long id);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    long countByProductId(Long productId);

    List<Sku> findByProductIdOrderByIdAsc(Long productId);

    Optional<Sku> findTopByProductIdOrderByIdDesc(Long productId);

    Optional<Sku> findByBarcode(String barcode);
}
