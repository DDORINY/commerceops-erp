package com.commerceops.erp.domain.product.repository;

import com.commerceops.erp.domain.product.entity.ProductDetailBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductDetailBlockRepository extends JpaRepository<ProductDetailBlock, Long> {

    List<ProductDetailBlock> findByProductIdOrderBySortOrderAscIdAsc(Long productId);

    List<ProductDetailBlock> findByProductIdAndVisibleTrueOrderBySortOrderAscIdAsc(Long productId);

    void deleteByProductId(Long productId);
}
