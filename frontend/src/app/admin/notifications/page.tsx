'use client';

import { useCallback, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import Pagination from '@/components/common/Pagination';
import { formatDateTime } from '@/lib/format';
import { notificationService, type ApiNotification } from '@/lib/services/notificationService';

const TYPE_LABEL: Record<ApiNotification['type'], string> = {
  ORDER_STATUS: '주문',
  INQUIRY_ANSWERED: '문의',
  RETURN_PROCESSED: '반품',
  SYSTEM: '시스템',
};

export default function AdminNotificationsPage() {
  const [notifications, setNotifications] = useState<ApiNotification[]>([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadNotifications = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const result = await notificationService.getAdminNotifications(page - 1, 20);
      setNotifications(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError(err instanceof Error ? err.message : '알림을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadNotifications();
    }, 0);
    return () => window.clearTimeout(timer);
  }, [loadNotifications]);

  return (
    <AdminLayout title="알림">
      <div className="space-y-5">
        <section className="border border-[#e8eaf0] bg-white p-4 sm:p-5">
          <h2 className="text-base font-semibold text-[#1a1f2e]">운영 알림</h2>
          <p className="mt-1 text-sm text-[#6f7a8a]">주문, 문의, 반품 및 시스템에서 발생한 최신 알림을 확인합니다.</p>
        </section>

        {error && <p className="border border-[#f0c7c7] bg-[#fff5f5] p-3 text-sm text-[#c44242]">{error}</p>}

        <section className="border border-[#e8eaf0] bg-white">
          {loading ? (
            <p className="p-10 text-center text-sm text-[#8a9bb5]">알림을 불러오는 중입니다.</p>
          ) : notifications.length === 0 ? (
            <p className="p-10 text-center text-sm text-[#8a9bb5]">등록된 알림이 없습니다.</p>
          ) : (
            <ul className="divide-y divide-[#edf0f5]">
              {notifications.map((notification) => (
                <li key={notification.id} className="flex flex-col gap-2 p-4 sm:flex-row sm:items-start sm:gap-4 sm:p-5">
                  <span className="w-fit shrink-0 bg-[#f0f3fa] px-2 py-1 text-xs font-medium text-[#52617a]">
                    {TYPE_LABEL[notification.type] ?? notification.type}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-semibold text-[#1a1f2e]">{notification.title}</p>
                    <p className="mt-1 break-words text-sm leading-6 text-[#566171]">{notification.message}</p>
                  </div>
                  <time className="shrink-0 text-xs text-[#8a9bb5]">{formatDateTime(notification.createdAt)}</time>
                </li>
              ))}
            </ul>
          )}
        </section>

        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </AdminLayout>
  );
}
