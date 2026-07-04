package com.commerceops.erp.domain.notification.controller;

import com.commerceops.erp.domain.notification.dto.NotificationResponse;
import com.commerceops.erp.domain.notification.dto.UnreadNotificationCountResponse;
import com.commerceops.erp.domain.notification.service.NotificationService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(notificationService.getMyNotifications(userDetails.getUser(), page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadNotificationCountResponse> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(notificationService.getUnreadCount(userDetails.getUser()));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        return ApiResponse.ok(notificationService.markRead(userDetails.getUser(), notificationId));
    }

    @PatchMapping("/read-all")
    public ApiResponse<UnreadNotificationCountResponse> markAllRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(notificationService.markAllRead(userDetails.getUser()));
    }
}
