import { apiClient, PageResponse } from '@/lib/api';

export interface ApiNotification {
  id: number;
  userId: number;
  type: 'ORDER_STATUS' | 'INQUIRY_ANSWERED' | 'RETURN_PROCESSED' | 'SYSTEM';
  title: string;
  message: string;
  targetType: string | null;
  targetId: number | null;
  read: boolean;
  readAt: string | null;
  createdAt: string;
}

export interface ApiUnreadNotificationCount {
  unreadCount: number;
}

export const notificationService = {
  getMyNotifications: (page = 0, size = 20) =>
    apiClient<PageResponse<ApiNotification>>(`/notifications?page=${page}&size=${size}`),

  getUnreadCount: () =>
    apiClient<ApiUnreadNotificationCount>('/notifications/unread-count'),

  markRead: (notificationId: number) =>
    apiClient<ApiNotification>(`/notifications/${notificationId}/read`, { method: 'PATCH' }),

  markAllRead: () =>
    apiClient<ApiUnreadNotificationCount>('/notifications/read-all', { method: 'PATCH' }),

  getAdminNotifications: (page = 0, size = 20, unreadOnly = false) =>
    apiClient<PageResponse<ApiNotification>>(
      `/admin/notifications?page=${page}&size=${size}&unreadOnly=${unreadOnly}`
    ),
};
