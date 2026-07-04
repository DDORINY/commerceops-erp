package com.commerceops.erp.domain.warehouse.repository;

import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<Warehouse> findByCodeIgnoreCase(String code);
    List<Warehouse> findAllByOrderByNameAsc();
}
