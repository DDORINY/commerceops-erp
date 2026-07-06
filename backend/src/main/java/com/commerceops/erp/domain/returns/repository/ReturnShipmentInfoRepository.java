package com.commerceops.erp.domain.returns.repository;

import com.commerceops.erp.domain.returns.entity.ReturnShipmentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReturnShipmentInfoRepository extends JpaRepository<ReturnShipmentInfo, Long> {

    Optional<ReturnShipmentInfo> findByReturnRequestId(Long returnRequestId);
}
