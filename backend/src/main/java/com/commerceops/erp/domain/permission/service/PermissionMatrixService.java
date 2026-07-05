package com.commerceops.erp.domain.permission.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.permission.dto.AdminMenuPermissionResponse;
import com.commerceops.erp.domain.permission.dto.AdminMenuPermissionUpdateRequest;
import com.commerceops.erp.domain.permission.dto.EffectivePermissionResponse;
import com.commerceops.erp.domain.permission.dto.PermissionGroupPermissionUpdateRequest;
import com.commerceops.erp.domain.permission.dto.PermissionResponse;
import com.commerceops.erp.domain.permission.entity.AdminMenuPermission;
import com.commerceops.erp.domain.permission.entity.Permission;
import com.commerceops.erp.domain.permission.entity.PermissionGroup;
import com.commerceops.erp.domain.permission.entity.PermissionGroupPermission;
import com.commerceops.erp.domain.permission.entity.UserPermissionGroup;
import com.commerceops.erp.domain.permission.repository.AdminMenuPermissionRepository;
import com.commerceops.erp.domain.permission.repository.PermissionGroupPermissionRepository;
import com.commerceops.erp.domain.permission.repository.PermissionGroupRepository;
import com.commerceops.erp.domain.permission.repository.PermissionRepository;
import com.commerceops.erp.domain.permission.repository.UserPermissionGroupRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.domain.user.repository.UserRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionMatrixService {

    private static final Map<UserRole, String> ROLE_DEFAULT_GROUP = Map.of(
            UserRole.SUPER_ADMIN, "SUPER_ADMIN_GROUP",
            UserRole.ADMIN, "ADMIN_GROUP",
            UserRole.MANAGER, "MANAGER_GROUP"
    );

    private final PermissionRepository permissionRepository;
    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionGroupPermissionRepository permissionGroupPermissionRepository;
    private final UserPermissionGroupRepository userPermissionGroupRepository;
    private final AdminMenuPermissionRepository adminMenuPermissionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public List<PermissionResponse> getPermissions() {
        return permissionRepository.findAllByOrderByDomainAscActionAscCodeAsc()
                .stream()
                .map(PermissionResponse::from)
                .toList();
    }

    public List<PermissionResponse> getGroupPermissions(Long groupId) {
        getGroup(groupId);
        return permissionGroupPermissionRepository.findByPermissionGroupIdOrderByPermissionDomainAscPermissionActionAscPermissionCodeAsc(groupId)
                .stream()
                .map(PermissionGroupPermission::getPermission)
                .map(PermissionResponse::from)
                .toList();
    }

    @Transactional
    public List<PermissionResponse> updateGroupPermissions(Long groupId, PermissionGroupPermissionUpdateRequest request, User actor) {
        PermissionGroup group = getGroup(groupId);
        if (!Boolean.TRUE.equals(group.getActive())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        List<PermissionResponse> before = getGroupPermissions(groupId);
        List<Permission> permissions = resolveActivePermissions(request.permissionIds());

        permissionGroupPermissionRepository.deleteByPermissionGroupId(groupId);
        for (Permission permission : permissions) {
            permissionGroupPermissionRepository.save(PermissionGroupPermission.create(group, permission));
        }

        auditLogService.record(
                actor,
                AuditActionType.PERMISSION_MATRIX_UPDATED,
                "PERMISSION_GROUP",
                group.getId(),
                summarizePermissionResponses(before),
                summarizePermissions(permissions),
                "권한 그룹 매트릭스를 수정했습니다: " + group.getCode()
        );
        return permissions.stream().map(PermissionResponse::from).toList();
    }

    public EffectivePermissionResponse getEffectivePermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new EffectivePermissionResponse(user.getId(), user.getRole().name(), calculateEffectivePermissionCodes(user));
    }

    public List<AdminMenuPermissionResponse> getMenuPermissions() {
        return adminMenuPermissionRepository.findAllByOrderBySortOrderAscIdAsc()
                .stream()
                .map(AdminMenuPermissionResponse::from)
                .toList();
    }

    @Transactional
    public List<AdminMenuPermissionResponse> updateMenuPermissions(AdminMenuPermissionUpdateRequest request, User actor) {
        List<AdminMenuPermissionResponse> before = getMenuPermissions();
        if (request.items() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        for (AdminMenuPermissionUpdateRequest.Item item : request.items()) {
            validatePermissionCode(item.requiredPermissionCode());
            AdminMenuPermission menuPermission = adminMenuPermissionRepository.findByMenuKey(item.menuKey())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
            menuPermission.update(
                    item.menuLabel(),
                    item.menuPath(),
                    item.requiredPermissionCode(),
                    item.visible(),
                    item.sortOrder()
            );
        }
        List<AdminMenuPermissionResponse> after = getMenuPermissions();
        auditLogService.record(
                actor,
                AuditActionType.MENU_PERMISSION_UPDATED,
                "ADMIN_MENU_PERMISSION",
                null,
                summarizeMenuPermissions(before),
                summarizeMenuPermissions(after),
                "관리자 메뉴 권한 기준을 수정했습니다."
        );
        return after;
    }

    private List<String> calculateEffectivePermissionCodes(User user) {
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return permissionRepository.findByActiveTrueOrderByDomainAscActionAscCodeAsc()
                    .stream()
                    .map(Permission::getCode)
                    .toList();
        }

        List<UserPermissionGroup> assignments = userPermissionGroupRepository.findByUserId(user.getId());
        List<Long> groupIds = new ArrayList<>(assignments.stream()
                .filter(assignment -> Boolean.TRUE.equals(assignment.getPermissionGroup().getActive()))
                .map(assignment -> assignment.getPermissionGroup().getId())
                .toList());

        if (groupIds.isEmpty() && ROLE_DEFAULT_GROUP.containsKey(user.getRole())) {
            permissionGroupRepository.findByCode(ROLE_DEFAULT_GROUP.get(user.getRole()))
                    .filter(group -> Boolean.TRUE.equals(group.getActive()))
                    .ifPresent(group -> groupIds.add(group.getId()));
        }

        if (groupIds.isEmpty()) {
            return List.of();
        }
        return permissionGroupPermissionRepository.findByPermissionGroupIdIn(groupIds)
                .stream()
                .filter(mapping -> Boolean.TRUE.equals(mapping.getPermissionGroup().getActive()))
                .filter(mapping -> Boolean.TRUE.equals(mapping.getPermission().getActive()))
                .map(mapping -> mapping.getPermission().getCode())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private PermissionGroup getGroup(Long groupId) {
        return permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private List<Permission> resolveActivePermissions(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return List.of();
        }
        Set<Long> uniqueIds = permissionIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Permission> permissions = new ArrayList<>();
        for (Long permissionId : uniqueIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
            if (!Boolean.TRUE.equals(permission.getActive())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            permissions.add(permission);
        }
        return permissions;
    }

    private void validatePermissionCode(String code) {
        permissionRepository.findByCode(code.trim().toUpperCase())
                .filter(permission -> Boolean.TRUE.equals(permission.getActive()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    private String summarizePermissionResponses(List<PermissionResponse> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "none";
        }
        return permissions.stream().map(PermissionResponse::code).collect(Collectors.joining(","));
    }

    private String summarizePermissions(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "none";
        }
        return permissions.stream().map(Permission::getCode).collect(Collectors.joining(","));
    }

    private String summarizeMenuPermissions(List<AdminMenuPermissionResponse> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "none";
        }
        return permissions.stream()
                .map(item -> item.menuKey() + ":" + item.requiredPermissionCode() + ":" + item.visible())
                .collect(Collectors.joining(","));
    }
}
