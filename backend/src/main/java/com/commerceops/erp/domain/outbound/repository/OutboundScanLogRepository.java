package com.commerceops.erp.domain.outbound.repository;

import com.commerceops.erp.domain.outbound.entity.OutboundScanLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboundScanLogRepository extends JpaRepository<OutboundScanLog, Long> {
}
