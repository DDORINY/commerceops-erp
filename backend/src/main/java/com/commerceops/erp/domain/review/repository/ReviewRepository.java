package com.commerceops.erp.domain.review.repository;

import com.commerceops.erp.domain.review.entity.Review;
import com.commerceops.erp.domain.review.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(
            value = "SELECT r FROM Review r JOIN FETCH r.product JOIN FETCH r.user " +
                    "WHERE r.product.id = :productId AND (r.status IS NULL OR r.status = :status) " +
                    "ORDER BY r.createdAt DESC",
            countQuery = "SELECT COUNT(r) FROM Review r " +
                    "WHERE r.product.id = :productId AND (r.status IS NULL OR r.status = :status)"
    )
    Page<Review> findVisibleByProductId(
            @Param("productId") Long productId,
            @Param("status") ReviewStatus status,
            Pageable pageable
    );

    @Query(
            value = "SELECT r FROM Review r " +
                    "JOIN FETCH r.product p " +
                    "JOIN FETCH r.user u " +
                    "WHERE (r.status IS NULL OR r.status != com.commerceops.erp.domain.review.enums.ReviewStatus.DELETED) " +
                    "AND (:rating IS NULL OR r.rating = :rating) " +
                    "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR u.name LIKE %:keyword% OR r.content LIKE %:keyword%)",
            countQuery = "SELECT COUNT(r) FROM Review r " +
                    "JOIN r.product p " +
                    "JOIN r.user u " +
                    "WHERE (r.status IS NULL OR r.status != com.commerceops.erp.domain.review.enums.ReviewStatus.DELETED) " +
                    "AND (:rating IS NULL OR r.rating = :rating) " +
                    "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR u.name LIKE %:keyword% OR r.content LIKE %:keyword%)"
    )
    Page<Review> findAllForAdmin(
            @Param("rating") Integer rating,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT r FROM Review r JOIN FETCH r.product JOIN FETCH r.user " +
            "WHERE r.user.id = :userId AND (r.status IS NULL OR r.status != :deletedStatus) " +
            "ORDER BY r.createdAt DESC")
    List<Review> findActiveByUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("deletedStatus") ReviewStatus deletedStatus
    );

    @Query("SELECT COUNT(r) > 0 FROM Review r " +
            "WHERE r.orderItemId = :orderItemId AND (r.status IS NULL OR r.status != :deletedStatus)")
    boolean existsActiveByOrderItemId(
            @Param("orderItemId") Long orderItemId,
            @Param("deletedStatus") ReviewStatus deletedStatus
    );

    Optional<Review> findByOrderItemId(Long orderItemId);
}
