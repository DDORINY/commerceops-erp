package com.commerceops.erp.domain.shipment.repository;

import com.commerceops.erp.domain.shipment.entity.ShipmentLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentLabelRepository extends JpaRepository<ShipmentLabel, Long> {

    List<ShipmentLabel> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);
}
