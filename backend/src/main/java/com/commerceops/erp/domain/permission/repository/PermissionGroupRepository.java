package com.commerceops.erp.domain.permission.repository;

import com.commerceops.erp.domain.permission.entity.PermissionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long> {
    List<PermissionGroup> findAllByOrderBySystemGroupDescNameAscIdAsc();
    Optional<PermissionGroup> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}
