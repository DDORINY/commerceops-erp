'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useCallback, useEffect, useRef, useState } from 'react';
import { authService } from '@/lib/services/authService';
import { clearAuth, getStoredUser } from '@/lib/auth';
import { formatDateTime } from '@/lib/format';
import { notificationService, type ApiNotification } from '@/lib/services/notificationService';

export default function AdminTopbar({ title, onMenuOpen }: { title: string; onMenuOpen?: () => void }) {
  const router = useRouter();
  const user = getStoredUser();
  const notificationRef = useRef<HTMLDivElement | null>(null);
  const [notificationOpen, setNotificationOpen] = useState(false);
  const [notifications, setNotifications] = useState<ApiNotification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [notificationLoading, setNotificationLoading] = useState(false);
  const [notificationError, setNotificationError] = useState('');

  const loadUnreadNotifications = useCallback(async () => {
    setNotificationLoading(true);
    setNotificationError('');
    try {
      const result = await notificationService.getAdminNotifications(0, 5, true);
      setNotifications(result.content);
      setUnreadCount(result.totalElements);
    } catch (error) {
      setNotificationError(error instanceof Error ? error.message : '알림을 불러오지 못했습니다.');
    } finally {
      setNotificationLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadUnreadNotifications();
    }, 0);
    return () => window.clearTimeout(timer);
  }, [loadUnreadNotifications]);

  useEffect(() => {
    if (!notificationOpen) return;
    const closeOnOutside = (event: MouseEvent) => {
      if (!notificationRef.current?.contains(event.target as Node)) setNotificationOpen(false);
    };
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setNotificationOpen(false);
    };
    document.addEventListener('mousedown', closeOnOutside);
    document.addEventListener('keydown', closeOnEscape);
    return () => {
      document.removeEventListener('mousedown', closeOnOutside);
      document.removeEventListener('keydown', closeOnEscape);
    };
  }, [notificationOpen]);

  const toggleNotifications = () => {
    setNotificationOpen((open) => {
      if (!open) void loadUnreadNotifications();
      return !open;
    });
  };

  const handleLogout = async () => {
    try {
      await authService.logout();
    } catch {
      // 클라이언트 토큰 삭제가 v0.2.1 로그아웃의 기준 동작이다.
    } finally {
      clearAuth();
      sessionStorage.setItem('authMessage', '로그아웃되었습니다.');
      router.replace('/login');
    }
  };

  return (
    <header className="h-[60px] bg-white border-b border-[#e8eaf0] flex items-center justify-between gap-2 px-3 sm:px-4 lg:px-6 flex-shrink-0">
      <div className="flex min-w-0 items-center gap-2">
        <button type="button" onClick={onMenuOpen} aria-label="관리자 메뉴 열기" className="-ml-1 grid h-10 w-10 shrink-0 place-items-center text-[#5f6b7a] lg:hidden">
          <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" /></svg>
        </button>
        <h1 className="truncate text-sm font-semibold text-[#1a1f2e] sm:text-base">{title}</h1>
      </div>
      <div className="flex shrink-0 items-center gap-2 sm:gap-4">
        {/* 알림 */}
        <div ref={notificationRef} className="relative">
          <button
            type="button"
            onClick={toggleNotifications}
            aria-label={`읽지 않은 알림 ${unreadCount}개`}
            aria-expanded={notificationOpen}
            aria-haspopup="menu"
            className="relative grid h-10 w-10 place-items-center text-[#8a9bb5] transition-colors hover:text-[#1a1f2e]"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
            {unreadCount > 0 && (
              <span className="absolute right-0.5 top-0.5 min-w-4 rounded-full bg-[#e76f8a] px-1 text-center text-[10px] font-semibold leading-4 text-white">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>

          {notificationOpen && (
            <div role="menu" className="fixed left-3 right-3 top-[56px] z-50 max-h-[min(70vh,440px)] overflow-hidden border border-[#dfe3ea] bg-white shadow-xl sm:absolute sm:left-auto sm:right-0 sm:top-11 sm:w-[360px]">
              <div className="flex items-center justify-between border-b border-[#edf0f5] px-4 py-3">
                <div>
                  <p className="text-sm font-semibold text-[#1a1f2e]">읽지 않은 알림</p>
                  <p className="mt-0.5 text-xs text-[#8a9bb5]">최신 알림 최대 5개</p>
                </div>
                <span className="text-xs font-semibold text-[#e15f7d]">{unreadCount}개</span>
              </div>

              <div className="max-h-[320px] overflow-y-auto">
                {notificationLoading ? (
                  <p className="px-4 py-10 text-center text-sm text-[#8a9bb5]">알림을 불러오는 중입니다.</p>
                ) : notificationError ? (
                  <div className="px-4 py-8 text-center">
                    <p className="text-sm text-[#c44242]">{notificationError}</p>
                    <button type="button" onClick={() => void loadUnreadNotifications()} className="mt-3 text-xs font-medium text-[#4c74e5]">다시 시도</button>
                  </div>
                ) : notifications.length === 0 ? (
                  <p className="px-4 py-10 text-center text-sm text-[#8a9bb5]">새로운 알림이 없습니다.</p>
                ) : (
                  <ul className="divide-y divide-[#edf0f5]">
                    {notifications.map((notification) => (
                      <li key={notification.id} className="px-4 py-3">
                        <p className="truncate text-sm font-semibold text-[#1a1f2e]">{notification.title}</p>
                        <p className="mt-1 line-clamp-2 text-xs leading-5 text-[#566171]">{notification.message}</p>
                        <time className="mt-1.5 block text-[11px] text-[#9aa3b1]">{formatDateTime(notification.createdAt)}</time>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <Link href="/admin/notifications" onClick={() => setNotificationOpen(false)} className="block border-t border-[#edf0f5] px-4 py-3 text-center text-xs font-semibold text-[#4c74e5] hover:bg-[#f8f9fb]">
                전체 알림 보기
              </Link>
            </div>
          )}
        </div>

        {/* 관리자 프로필 */}
        <div className="hidden items-center gap-2.5 sm:flex">
          <div className="w-8 h-8 bg-[#1a1f2e] rounded-full flex items-center justify-center">
            <span className="text-white text-xs font-medium">관</span>
          </div>
          <div className="hidden md:block">
            <p className="text-xs font-medium text-[#1a1f2e]">{user?.name ?? '관리자'}</p>
            <p className="text-[10px] text-[#8a9bb5]">{user?.email ?? '관리자'}</p>
          </div>
        </div>
        <button
          type="button"
          onClick={handleLogout}
          className="h-8 px-2 sm:px-3 border border-[#d8dde8] text-[11px] sm:text-xs text-[#5f6b7a] hover:border-[#aeb8c8] hover:text-[#1a1f2e] transition-colors"
        >
          로그아웃
        </button>
      </div>
    </header>
  );
}
