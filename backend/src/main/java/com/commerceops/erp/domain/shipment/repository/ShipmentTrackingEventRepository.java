package com.commerceops.erp.domain.shipment.repository;

import com.commerceops.erp.domain.shipment.entity.ShipmentTrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentTrackingEventRepository extends JpaRepository<ShipmentTrackingEvent, Long> {

    List<ShipmentTrackingEvent> findByShipmentIdOrderByEventAtDesc(Long shipmentId);
}
