package com.commerceops.erp.domain.user.service;

import com.commerceops.erp.domain.user.dto.UserSummaryResponse;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.domain.user.repository.UserRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public PageResponse<UserSummaryResponse> getUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        return PageResponse.from(
                userRepository.findAllForAdmin(kw, pageable).map(this::toSummary)
        );
    }

    @Transactional
    public UserSummaryResponse changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserRole newRole;
        try {
            newRole = UserRole.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (newRole == UserRole.SUPER_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        user.changeRole(newRole);
        return toSummary(user);
    }

    private UserSummaryResponse toSummary(User user) {
        return UserSummaryResponse.from(
                user,
                orderRepository.countByUser(user),
                orderRepository.sumTotalPriceByUser(user)
        );
    }
}
