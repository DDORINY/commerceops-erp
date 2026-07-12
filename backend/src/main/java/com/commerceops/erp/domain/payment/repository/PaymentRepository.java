package com.commerceops.erp.domain.payment.repository;

import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.payment.entity.Payment;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT SUM(p.paidAmount) FROM Payment p WHERE p.paymentStatus = :status")
    Optional<Long> sumPaidAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT SUM(p.paidAmount) FROM Payment p WHERE p.paymentStatus = :status " +
           "AND p.createdAt >= :startOfDay AND p.createdAt < :endOfDay")
    Optional<Long> sumPaidAmountByStatusAndDate(
            @Param("status") PaymentStatus status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query(nativeQuery = true,
           value = "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS date_label, " +
                   "COALESCE(SUM(paid_amount), 0) AS sales_amount, COUNT(*) AS order_count " +
                   "FROM payments " +
                   "WHERE payment_status = 'PAID' AND created_at >= :startDate AND created_at < :endDate " +
                   "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') " +
                   "ORDER BY date_label ASC")
    List<Object[]> findDailySales(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(nativeQuery = true,
           value = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS date_label, " +
                   "COALESCE(SUM(paid_amount), 0) AS sales_amount, COUNT(*) AS order_count " +
                   "FROM payments " +
                   "WHERE payment_status = 'PAID' AND created_at >= :startDate AND created_at < :endDate " +
                   "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
                   "ORDER BY date_label ASC")
    List<Object[]> findMonthlySales(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(
            value = """
            SELECT p FROM Payment p JOIN FETCH p.order o
            WHERE p.paymentStatus = :status
              AND NOT EXISTS (
                  SELECT t.id FROM AccountingTransaction t
                  WHERE t.referenceType = :referenceType
                    AND t.referenceId = p.id
                    AND t.type = :transactionType
              )
            ORDER BY p.updatedAt DESC
            """,
            countQuery = """
            SELECT COUNT(p) FROM Payment p
            WHERE p.paymentStatus = :status
              AND NOT EXISTS (
                  SELECT t.id FROM AccountingTransaction t
                  WHERE t.referenceType = :referenceType
                    AND t.referenceId = p.id
                    AND t.type = :transactionType
              )
            """
    )
    Page<Payment> findMissingAccountingTransactions(
            @Param("status") PaymentStatus status,
            @Param("referenceType") AccountingReferenceType referenceType,
            @Param("transactionType") AccountingTransactionType transactionType,
            Pageable pageable
    );
}
