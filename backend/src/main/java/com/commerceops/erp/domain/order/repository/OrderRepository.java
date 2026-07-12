package com.commerceops.erp.domain.order.repository;

import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @Query(
        value = "SELECT o FROM Order o JOIN FETCH o.user " +
                "WHERE (:status IS NULL OR o.status = :status) " +
                "AND (:keyword IS NULL OR o.orderNumber LIKE %:keyword% OR o.receiverName LIKE %:keyword%)",
        countQuery = "SELECT COUNT(o) FROM Order o " +
                     "WHERE (:status IS NULL OR o.status = :status) " +
                     "AND (:keyword IS NULL OR o.orderNumber LIKE %:keyword% OR o.receiverName LIKE %:keyword%)"
    )
    Page<Order> findAllForAdmin(
            @Param("status") OrderStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay")
    Long countTodayOrders(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    Long countByStatus(OrderStatus status);

    Long countByUser(User user);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.user = :user")
    Long sumTotalPriceByUser(@Param("user") User user);

    @Query("""
            SELECT o FROM Order o
            WHERE o.status IN :statuses
              AND NOT EXISTS (
                  SELECT t.id FROM AccountingTransaction t
                  WHERE t.referenceType = :referenceType
                    AND t.referenceId = o.id
                    AND t.type = :transactionType
              )
            ORDER BY o.createdAt DESC
            """)
    Page<Order> findMissingAccountingTransactions(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("referenceType") AccountingReferenceType referenceType,
            @Param("transactionType") AccountingTransactionType transactionType,
            Pageable pageable
    );
}
