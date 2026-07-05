package com.commerceops.erp.domain.product.repository;

import com.commerceops.erp.domain.product.entity.ProductOperationNote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOperationNoteRepository extends JpaRepository<ProductOperationNote, Long> {

    List<ProductOperationNote> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
}
