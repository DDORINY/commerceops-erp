package com.commerceops.erp.domain.notification.service;

import com.commerceops.erp.domain.notification.dto.NotificationResponse;
import com.commerceops.erp.domain.notification.dto.UnreadNotificationCountResponse;
import com.commerceops.erp.domain.notification.entity.Notification;
import com.commerceops.erp.domain.notification.enums.NotificationType;
import com.commerceops.erp.domain.notification.repository.NotificationRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notifyUser(User user, NotificationType type, String title, String message,
                           String targetType, Long targetId) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .targetType(targetType)
                .targetId(targetId)
                .build());
    }

    public PageResponse<NotificationResponse> getMyNotifications(User user, int page, int size) {
        return PageResponse.from(notificationRepository
                .findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(NotificationResponse::from));
    }

    public PageResponse<NotificationResponse> getAdminNotifications(int page, int size, boolean unreadOnly) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from((unreadOnly
                ? notificationRepository.findByReadAtIsNull(pageable)
                : notificationRepository.findAll(pageable))
                .map(NotificationResponse::from));
    }

    public UnreadNotificationCountResponse getUnreadCount(User user) {
        return new UnreadNotificationCountResponse(notificationRepository.countByUserAndReadAtIsNull(user));
    }

    @Transactional
    public NotificationResponse markRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        notification.markRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public UnreadNotificationCountResponse markAllRead(User user) {
        notificationRepository.markAllReadByUser(user);
        return getUnreadCount(user);
    }
}
