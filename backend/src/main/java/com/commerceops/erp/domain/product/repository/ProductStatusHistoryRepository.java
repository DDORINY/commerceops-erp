package com.commerceops.erp.domain.product.repository;

import com.commerceops.erp.domain.product.entity.ProductStatusHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductStatusHistoryRepository extends JpaRepository<ProductStatusHistory, Long> {

    List<ProductStatusHistory> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
}
