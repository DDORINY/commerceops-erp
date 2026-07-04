package com.commerceops.erp.domain.notification.dto;

import com.commerceops.erp.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String type,
        String title,
        String message,
        String targetType,
        Long targetId,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getReadAt() != null,
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
