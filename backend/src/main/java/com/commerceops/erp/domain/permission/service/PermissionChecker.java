package com.commerceops.erp.domain.permission.service;

import com.commerceops.erp.domain.permission.dto.EffectivePermissionResponse;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionChecker {

    private static final String FORBIDDEN_MESSAGE = "해당 작업을 수행할 권한이 없습니다. 관리자에게 권한을 요청하세요.";

    private final PermissionMatrixService permissionMatrixService;

    public void require(CustomUserDetails userDetails, String permissionCode) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        require(userDetails.getUser(), permissionCode);
    }

    public void require(User user, String permissionCode) {
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }
        EffectivePermissionResponse effectivePermissions = permissionMatrixService.getEffectivePermissions(user);
        if (!effectivePermissions.permissionCodes().contains(permissionCode)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, FORBIDDEN_MESSAGE);
        }
    }
}
