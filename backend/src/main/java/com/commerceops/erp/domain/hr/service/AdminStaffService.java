package com.commerceops.erp.domain.hr.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.hr.dto.StaffActiveUpdateRequest;
import com.commerceops.erp.domain.hr.dto.StaffCreateRequest;
import com.commerceops.erp.domain.hr.dto.StaffProfileResponse;
import com.commerceops.erp.domain.hr.dto.StaffStatusUpdateRequest;
import com.commerceops.erp.domain.hr.dto.StaffUpdateRequest;
import com.commerceops.erp.domain.hr.entity.Department;
import com.commerceops.erp.domain.hr.entity.Position;
import com.commerceops.erp.domain.hr.entity.StaffProfile;
import com.commerceops.erp.domain.hr.enums.EmploymentStatus;
import com.commerceops.erp.domain.hr.repository.DepartmentRepository;
import com.commerceops.erp.domain.hr.repository.PositionRepository;
import com.commerceops.erp.domain.hr.repository.StaffProfileRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.domain.user.enums.UserStatus;
import com.commerceops.erp.domain.user.repository.UserRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStaffService {

    private final StaffProfileRepository staffProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public PageResponse<StaffProfileResponse> getStaff(
            String keyword,
            Long departmentId,
            Long positionId,
            EmploymentStatus employmentStatus,
            Boolean active,
            UserRole role,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return PageResponse.from(
                staffProfileRepository.findAllForAdmin(
                        kw,
                        departmentId,
                        positionId,
                        employmentStatus,
                        active,
                        role,
                        pageable
                ).map(StaffProfileResponse::from)
        );
    }

    public StaffProfileResponse getStaffDetail(Long staffId) {
        return StaffProfileResponse.from(getStaffProfile(staffId));
    }

    @Transactional
    public StaffProfileResponse createStaff(StaffCreateRequest request, User actor) {
        validateStaffRole(request.role());
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        validateEmployeeNo(request.employeeNo(), null);

        User user = User.builder()
                .email(request.email().trim())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name().trim())
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        StaffProfile profile = StaffProfile.create(
                savedUser,
                resolveDepartment(request.departmentId()),
                resolvePosition(request.positionId()),
                request.employeeNo(),
                request.employmentStatus(),
                request.joinedAt(),
                request.active()
        );
        StaffProfile savedProfile = staffProfileRepository.save(profile);
        auditLogService.record(
                actor,
                AuditActionType.STAFF_CREATED,
                "STAFF",
                savedProfile.getId(),
                null,
                savedProfile.getEmploymentStatus().name(),
                "직원 계정이 생성되었습니다: " + savedUser.getEmail()
        );
        return StaffProfileResponse.from(savedProfile);
    }

    @Transactional
    public StaffProfileResponse updateStaff(Long staffId, StaffUpdateRequest request, User actor) {
        StaffProfile profile = getStaffProfile(staffId);
        User user = profile.getUser();
        UserRole beforeRole = user.getRole();
        EmploymentStatus beforeStatus = profile.getEmploymentStatus();
        Boolean beforeActive = profile.getActive();

        if (request.role() != null) {
            validateStaffRole(request.role());
            validateSuperAdminChange(user, request.role(), actor);
            user.changeRole(request.role());
        }
        user.updateProfile(request.name());
        validateEmployeeNo(request.employeeNo(), staffId);
        validateLastSuperAdminProfileChange(user, request.employmentStatus(), request.active(), actor);
        profile.update(
                resolveDepartment(request.departmentId()),
                resolvePosition(request.positionId()),
                request.employeeNo(),
                request.employmentStatus(),
                request.joinedAt(),
                request.leftAt(),
                request.active()
        );
        auditLogService.record(
                actor,
                AuditActionType.STAFF_UPDATED,
                "STAFF",
                profile.getId(),
                beforeRole + "/" + beforeStatus + "/" + beforeActive,
                user.getRole() + "/" + profile.getEmploymentStatus() + "/" + profile.getActive(),
                "직원 정보가 수정되었습니다: " + user.getEmail()
        );
        return StaffProfileResponse.from(profile);
    }

    @Transactional
    public StaffProfileResponse updateEmploymentStatus(Long staffId, StaffStatusUpdateRequest request, User actor) {
        StaffProfile profile = getStaffProfile(staffId);
        EmploymentStatus before = profile.getEmploymentStatus();
        validateLastSuperAdminProfileChange(profile.getUser(), request.employmentStatus(), null, actor);
        profile.changeEmploymentStatus(request.employmentStatus(), request.leftAt());
        auditLogService.record(
                actor,
                AuditActionType.STAFF_STATUS_CHANGED,
                "STAFF",
                profile.getId(),
                before.name(),
                profile.getEmploymentStatus().name(),
                "직원 재직 상태가 변경되었습니다: " + profile.getUser().getEmail()
        );
        return StaffProfileResponse.from(profile);
    }

    @Transactional
    public StaffProfileResponse updateActive(Long staffId, StaffActiveUpdateRequest request, User actor) {
        StaffProfile profile = getStaffProfile(staffId);
        Boolean before = profile.getActive();
        validateLastSuperAdminProfileChange(profile.getUser(), null, request.active(), actor);
        profile.changeActive(request.active());
        auditLogService.record(
                actor,
                AuditActionType.STAFF_ACTIVE_CHANGED,
                "STAFF",
                profile.getId(),
                String.valueOf(before),
                String.valueOf(profile.getActive()),
                "직원 프로필 활성 상태가 변경되었습니다: " + profile.getUser().getEmail()
        );
        return StaffProfileResponse.from(profile);
    }

    private StaffProfile getStaffProfile(Long staffId) {
        return staffProfileRepository.findById(staffId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private Position resolvePosition(Long positionId) {
        if (positionId == null) {
            return null;
        }
        return positionRepository.findById(positionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private void validateEmployeeNo(String employeeNo, Long staffId) {
        if (employeeNo == null || employeeNo.isBlank()) {
            return;
        }
        boolean duplicated = staffId == null
                ? staffProfileRepository.existsByEmployeeNo(employeeNo.trim())
                : staffProfileRepository.existsByEmployeeNoAndIdNot(employeeNo.trim(), staffId);
        if (duplicated) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateStaffRole(UserRole role) {
        if (role == null || role == UserRole.USER) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateSuperAdminChange(User targetUser, UserRole newRole, User actor) {
        if (targetUser.getId().equals(actor.getId()) && targetUser.getRole() == UserRole.SUPER_ADMIN && newRole != UserRole.SUPER_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (targetUser.getRole() == UserRole.SUPER_ADMIN && newRole != UserRole.SUPER_ADMIN && isLastActiveSuperAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateLastSuperAdminProfileChange(User targetUser, EmploymentStatus employmentStatus, Boolean active, User actor) {
        if (targetUser.getRole() != UserRole.SUPER_ADMIN) {
            return;
        }
        boolean disabling = active != null && !active;
        boolean resigning = employmentStatus == EmploymentStatus.RESIGNED;
        if (!disabling && !resigning) {
            return;
        }
        if (targetUser.getId().equals(actor.getId()) || isLastActiveSuperAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isLastActiveSuperAdmin() {
        return userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE) <= 1;
    }
}
