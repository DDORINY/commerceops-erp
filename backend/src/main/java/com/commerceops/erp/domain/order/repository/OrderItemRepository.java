package com.commerceops.erp.domain.order.repository;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order = :order")
    List<OrderItem> findAllByOrderWithProduct(@Param("order") Order order);

    Long countByOrder(Order order);

    @Query(nativeQuery = true,
           value = "SELECT oi.product_id, oi.product_name, " +
                   "SUM(oi.quantity) AS order_count, " +
                   "SUM(oi.price * oi.quantity) AS sales_amount " +
                   "FROM order_items oi " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "WHERE o.status IN ('PAID', 'PREPARING', 'SHIPPING', 'COMPLETED') " +
                   "GROUP BY oi.product_id, oi.product_name " +
                   "ORDER BY order_count DESC")
    List<Object[]> findTopProducts(Pageable pageable);
}
