package com.commerceops.erp.domain.audit.repository;

import com.commerceops.erp.domain.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTargetTypeOrderByCreatedAtDesc(String targetType, Pageable pageable);
}
