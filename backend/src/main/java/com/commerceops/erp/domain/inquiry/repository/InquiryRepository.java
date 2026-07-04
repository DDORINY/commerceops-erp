package com.commerceops.erp.domain.inquiry.repository;

import com.commerceops.erp.domain.inquiry.entity.Inquiry;
import com.commerceops.erp.domain.inquiry.enums.InquiryStatus;
import com.commerceops.erp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByUserOrderByCreatedAtDesc(User user);

    List<Inquiry> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query(
        value = "SELECT i FROM Inquiry i LEFT JOIN FETCH i.product " +
                "JOIN FETCH i.user u " +
                "WHERE (:status IS NULL OR i.status = :status) " +
                "AND (:keyword IS NULL OR i.subject LIKE %:keyword% OR u.name LIKE %:keyword%)",
        countQuery = "SELECT COUNT(i) FROM Inquiry i JOIN i.user u " +
                     "WHERE (:status IS NULL OR i.status = :status) " +
                     "AND (:keyword IS NULL OR i.subject LIKE %:keyword% OR u.name LIKE %:keyword%)"
    )
    Page<Inquiry> findAllForAdmin(
            @Param("status") InquiryStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
