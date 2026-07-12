package com.commerceops.erp.domain.shipping.repository;

import com.commerceops.erp.domain.shipping.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CarrierRepository extends JpaRepository<Carrier, Long>, JpaSpecificationExecutor<Carrier> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<Carrier> findFirstByCodeIgnoreCaseOrNameIgnoreCase(String code, String name);
}
