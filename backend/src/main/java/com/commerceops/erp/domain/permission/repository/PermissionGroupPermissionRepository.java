package com.commerceops.erp.domain.permission.repository;

import com.commerceops.erp.domain.permission.entity.PermissionGroupPermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionGroupPermissionRepository extends JpaRepository<PermissionGroupPermission, Long> {
    @EntityGraph(attributePaths = {"permission"})
    List<PermissionGroupPermission> findByPermissionGroupIdOrderByPermissionDomainAscPermissionActionAscPermissionCodeAsc(Long permissionGroupId);

    @EntityGraph(attributePaths = {"permission", "permissionGroup"})
    List<PermissionGroupPermission> findByPermissionGroupIdIn(List<Long> permissionGroupIds);

    void deleteByPermissionGroupId(Long permissionGroupId);
}
