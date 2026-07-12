package com.commerceops.erp.domain.accounting.repository;

import com.commerceops.erp.domain.accounting.entity.ShippingCostEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingCostEntryRepository extends JpaRepository<ShippingCostEntry, Long> {

    Optional<ShippingCostEntry> findByShipmentId(Long shipmentId);

    Page<ShippingCostEntry> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
