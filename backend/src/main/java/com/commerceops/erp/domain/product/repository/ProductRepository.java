package com.commerceops.erp.domain.product.repository;

import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdForUpdate(@Param("productId") Long productId);

    Long countByStatus(ProductStatus status);

    Long countByStatusNot(ProductStatus status);

    Long countByStockQuantityLessThanEqualAndStatusNot(int stockQuantity, ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.status != :excludedStatus " +
           "ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(
            @Param("threshold") int threshold,
            @Param("excludedStatus") ProductStatus excludedStatus,
            Pageable pageable
    );
}
