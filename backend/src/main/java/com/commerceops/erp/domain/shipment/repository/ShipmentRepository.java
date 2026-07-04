package com.commerceops.erp.domain.shipment.repository;

import com.commerceops.erp.domain.shipment.entity.Shipment;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrderId(Long orderId);

    @Query(
        value = "SELECT s FROM Shipment s JOIN FETCH s.order o JOIN FETCH o.user " +
                "WHERE (:status IS NULL OR s.status = :status) " +
                "AND (:keyword IS NULL OR o.orderNumber LIKE %:keyword% OR o.receiverName LIKE %:keyword%)",
        countQuery = "SELECT COUNT(s) FROM Shipment s JOIN s.order o " +
                     "WHERE (:status IS NULL OR s.status = :status) " +
                     "AND (:keyword IS NULL OR o.orderNumber LIKE %:keyword% OR o.receiverName LIKE %:keyword%)"
    )
    Page<Shipment> findAllForAdmin(
            @Param("status") ShipmentStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
