package com.commerceops.erp.domain.permission.repository;

import com.commerceops.erp.domain.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findAllByOrderByDomainAscActionAscCodeAsc();
    List<Permission> findByActiveTrueOrderByDomainAscActionAscCodeAsc();
    Optional<Permission> findByCode(String code);
}
