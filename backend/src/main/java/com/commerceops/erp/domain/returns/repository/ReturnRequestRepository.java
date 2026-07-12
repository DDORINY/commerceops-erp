package com.commerceops.erp.domain.returns.repository;

import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import com.commerceops.erp.domain.returns.entity.ReturnRequest;
import com.commerceops.erp.domain.returns.enums.ReturnStatus;
import com.commerceops.erp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByUserOrderByCreatedAtDesc(User user);

    boolean existsByOrderIdAndStatusNot(Long orderId, ReturnStatus status);

    @Query(
        value = "SELECT r FROM ReturnRequest r JOIN FETCH r.order o JOIN FETCH r.user u " +
                "WHERE (:status IS NULL OR r.status = :status) " +
                "AND (:keyword IS NULL OR o.orderNumber LIKE %:keyword% OR u.name LIKE %:keyword%)",
        countQuery = "SELECT COUNT(r) FROM ReturnRequest r JOIN r.order o JOIN r.user u " +
                     "WHERE (:status IS NULL OR r.status = :status) " +
                     "AND (:keyword IS NULL OR o.orderNumber LIKE %:keyword% OR u.name LIKE %:keyword%)"
    )
    Page<ReturnRequest> findAllForAdmin(
            @Param("status") ReturnStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(
            value = """
            SELECT r FROM ReturnRequest r JOIN FETCH r.order o
            WHERE r.status = :status
              AND NOT EXISTS (
                  SELECT t.id FROM AccountingTransaction t
                  WHERE t.referenceType = :referenceType
                    AND t.referenceId = r.id
                    AND t.type = :transactionType
              )
            ORDER BY r.updatedAt DESC
            """,
            countQuery = """
            SELECT COUNT(r) FROM ReturnRequest r
            WHERE r.status = :status
              AND NOT EXISTS (
                  SELECT t.id FROM AccountingTransaction t
                  WHERE t.referenceType = :referenceType
                    AND t.referenceId = r.id
                    AND t.type = :transactionType
              )
            """
    )
    Page<ReturnRequest> findMissingAccountingTransactions(
            @Param("status") ReturnStatus status,
            @Param("referenceType") AccountingReferenceType referenceType,
            @Param("transactionType") AccountingTransactionType transactionType,
            Pageable pageable
    );
}
