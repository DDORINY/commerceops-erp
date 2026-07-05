package com.commerceops.erp.domain.permission.repository;

import com.commerceops.erp.domain.permission.entity.UserPermissionGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserPermissionGroupRepository extends JpaRepository<UserPermissionGroup, Long> {
    @EntityGraph(attributePaths = {"permissionGroup"})
    List<UserPermissionGroup> findByUserIdOrderByPermissionGroupNameAsc(Long userId);

    List<UserPermissionGroup> findByUserId(Long userId);

    boolean existsByPermissionGroupId(Long permissionGroupId);

    void deleteByUserIdAndPermissionGroupIdNotIn(Long userId, Collection<Long> permissionGroupIds);

    void deleteByUserId(Long userId);
}
