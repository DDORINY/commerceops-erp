package com.commerceops.erp.domain.warehouse.repository;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.warehouse.entity.StockReservation;
import com.commerceops.erp.domain.warehouse.enums.StockReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    boolean existsByOrder(Order order);

    @Query("SELECT r FROM StockReservation r JOIN FETCH r.warehouseStock s JOIN FETCH s.warehouse " +
            "WHERE r.order = :order AND r.status = :status ORDER BY r.id")
    List<StockReservation> findAllByOrderAndStatus(
            @Param("order") Order order,
            @Param("status") StockReservationStatus status
    );
}
