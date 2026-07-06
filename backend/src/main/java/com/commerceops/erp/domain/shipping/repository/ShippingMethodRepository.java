package com.commerceops.erp.domain.shipping.repository;

import com.commerceops.erp.domain.shipping.entity.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long>, JpaSpecificationExecutor<ShippingMethod> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
