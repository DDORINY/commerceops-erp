package com.commerceops.erp.domain.permission.repository;

import com.commerceops.erp.domain.permission.entity.AdminMenuPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminMenuPermissionRepository extends JpaRepository<AdminMenuPermission, Long> {
    List<AdminMenuPermission> findAllByOrderBySortOrderAscIdAsc();
    Optional<AdminMenuPermission> findByMenuKey(String menuKey);
}
