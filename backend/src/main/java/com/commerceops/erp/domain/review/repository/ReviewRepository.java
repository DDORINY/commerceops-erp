package com.commerceops.erp.domain.review.repository;

import com.commerceops.erp.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    @Query(
            value = "SELECT r FROM Review r " +
                    "JOIN FETCH r.product p " +
                    "JOIN FETCH r.user u " +
                    "WHERE (:rating IS NULL OR r.rating = :rating) " +
                    "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR u.name LIKE %:keyword% OR r.content LIKE %:keyword%)",
            countQuery = "SELECT COUNT(r) FROM Review r " +
                    "JOIN r.product p " +
                    "JOIN r.user u " +
                    "WHERE (:rating IS NULL OR r.rating = :rating) " +
                    "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR u.name LIKE %:keyword% OR r.content LIKE %:keyword%)"
    )
    Page<Review> findAllForAdmin(
            @Param("rating") Integer rating,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByOrderItemId(Long orderItemId);

    Optional<Review> findByOrderItemId(Long orderItemId);
}
