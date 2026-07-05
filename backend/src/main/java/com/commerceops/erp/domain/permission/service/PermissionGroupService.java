package com.commerceops.erp.domain.permission.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.permission.dto.PermissionGroupActiveUpdateRequest;
import com.commerceops.erp.domain.permission.dto.PermissionGroupCreateRequest;
import com.commerceops.erp.domain.permission.dto.PermissionGroupResponse;
import com.commerceops.erp.domain.permission.dto.PermissionGroupUpdateRequest;
import com.commerceops.erp.domain.permission.dto.UserPermissionGroupResponse;
import com.commerceops.erp.domain.permission.dto.UserPermissionGroupUpdateRequest;
import com.commerceops.erp.domain.permission.entity.PermissionGroup;
import com.commerceops.erp.domain.permission.entity.UserPermissionGroup;
import com.commerceops.erp.domain.permission.repository.PermissionGroupRepository;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionGroupService {

    private final PermissionGroupRepository permissionGroupRepository;
    private final UserPermissionGroupRepository userPermissionGroupRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public List<PermissionGroupResponse> getPermissionGroups() {
        return permissionGroupRepository.findAllByOrderBySystemGroupDescNameAscIdAsc()
                .stream()
                .map(PermissionGroupResponse::from)
                .toList();
    }

    public PermissionGroupResponse getPermissionGroup(Long groupId) {
        return PermissionGroupResponse.from(getGroup(groupId));
    }

    @Transactional
    public PermissionGroupResponse createPermissionGroup(PermissionGroupCreateRequest request, User actor) {
        String code = PermissionGroup.normalizeCode(request.code());
        if (permissionGroupRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        PermissionGroup saved = permissionGroupRepository.save(
                PermissionGroup.create(request.name(), code, request.description(), false, request.active())
        );
        auditLogService.record(
                actor,
                AuditActionType.PERMISSION_GROUP_CREATED,
                "PERMISSION_GROUP",
                saved.getId(),
                null,
                saved.getCode(),
                "권한 그룹을 생성했습니다: " + saved.getName()
        );
        return PermissionGroupResponse.from(saved);
    }

    @Transactional
    public PermissionGroupResponse updatePermissionGroup(Long groupId, PermissionGroupUpdateRequest request, User actor) {
        PermissionGroup group = getGroup(groupId);
        String before = group.getName() + "/" + group.getDescription();
        group.update(request.name(), request.description());
        auditLogService.record(
                actor,
                AuditActionType.PERMISSION_GROUP_UPDATED,
                "PERMISSION_GROUP",
                group.getId(),
                before,
                group.getName() + "/" + group.getDescription(),
                "권한 그룹 정보를 수정했습니다: " + group.getCode()
        );
        return PermissionGroupResponse.from(group);
    }

    @Transactional
    public PermissionGroupResponse updatePermissionGroupActive(Long groupId, PermissionGroupActiveUpdateRequest request, User actor) {
        PermissionGroup group = getGroup(groupId);
        if (Boolean.TRUE.equals(group.getSystemGroup()) && !request.active()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!request.active() && userPermissionGroupRepository.existsByPermissionGroupId(group.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Boolean before = group.getActive();
        group.changeActive(request.active());
        auditLogService.record(
                actor,
                AuditActionType.PERMISSION_GROUP_ACTIVE_CHANGED,
                "PERMISSION_GROUP",
                group.getId(),
                String.valueOf(before),
                String.valueOf(group.getActive()),
                "권한 그룹 활성 상태를 변경했습니다: " + group.getCode()
        );
        return PermissionGroupResponse.from(group);
    }

    public List<UserPermissionGroupResponse> getUserPermissionGroups(Long userId) {
        validateAdminUser(userId);
        return userPermissionGroupRepository.findByUserIdOrderByPermissionGroupNameAsc(userId)
                .stream()
                .map(UserPermissionGroupResponse::from)
                .toList();
    }

    @Transactional
    public List<UserPermissionGroupResponse> updateUserPermissionGroups(
            Long userId,
            UserPermissionGroupUpdateRequest request,
            User actor
    ) {
        User user = validateAdminUser(userId);
        List<Long> groupIds = normalizeIds(request.permissionGroupIds());
        List<PermissionGroup> groups = resolveActiveGroups(groupIds);
        List<UserPermissionGroupResponse> before = getUserPermissionGroups(userId);

        userPermissionGroupRepository.deleteByUserId(userId);
        List<UserPermissionGroup> savedAssignments = new ArrayList<>();
        for (PermissionGroup group : groups) {
            savedAssignments.add(userPermissionGroupRepository.save(UserPermissionGroup.create(user, group, actor)));
        }

        auditLogService.record(
                actor,
                AuditActionType.USER_PERMISSION_GROUPS_UPDATED,
                "USER",
                user.getId(),
                summarizeAssignments(before),
                summarizeGroups(groups),
                "사용자 권한 그룹 할당을 변경했습니다: " + user.getEmail()
        );
        return savedAssignments.stream()
                .map(UserPermissionGroupResponse::from)
                .toList();
    }

    private PermissionGroup getGroup(Long groupId) {
        return permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private User validateAdminUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() == UserRole.USER) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return user;
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<Long> unique = new LinkedHashSet<>(ids);
        return unique.stream().filter(id -> id != null && id > 0).toList();
    }

    private List<PermissionGroup> resolveActiveGroups(List<Long> groupIds) {
        List<PermissionGroup> groups = new ArrayList<>();
        for (Long groupId : groupIds) {
            PermissionGroup group = getGroup(groupId);
            if (!Boolean.TRUE.equals(group.getActive())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            groups.add(group);
        }
        return groups;
    }

    private String summarizeAssignments(List<UserPermissionGroupResponse> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return "none";
        }
        return assignments.stream()
                .map(UserPermissionGroupResponse::permissionGroupCode)
                .reduce((left, right) -> left + "," + right)
                .orElse("none");
    }

    private String summarizeGroups(List<PermissionGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return "none";
        }
        return groups.stream()
                .map(PermissionGroup::getCode)
                .reduce((left, right) -> left + "," + right)
                .orElse("none");
    }
}
