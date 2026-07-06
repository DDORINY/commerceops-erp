'use client';

import { ReactNode, useEffect, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import AdminSidebarV2 from './AdminSidebarV2';
import AdminTopbar from './AdminTopbar';
import { canAccessByRole, findActiveAdminMenuItem, isAdminRole } from '@/lib/adminMenu';
import { getAccessToken, getUserRole } from '@/lib/auth';
import { permissionGroupService, type AdminMenuPermission } from '@/lib/services/permissionGroupService';

interface AdminLayoutProps {
  children: ReactNode;
  title: string;
}

type GuardStatus = 'checking' | 'allowed' | 'forbidden';

function canAccessByPermission(
  role: ReturnType<typeof getUserRole>,
  pathname: string,
  currentQuery: string,
  permissionCodes: string[],
  menuPermissions: AdminMenuPermission[]
) {
  if (!isAdminRole(role)) return false;
  const activeItem = findActiveAdminMenuItem(pathname, currentQuery);
  if (!activeItem) return true;

  const menuPermission = menuPermissions.find((item) => item.menuKey === activeItem.menuKey);
  if (!menuPermission) return canAccessByRole(activeItem, role);
  if (!menuPermission.visible) return false;
  if (role === 'SUPER_ADMIN') return true;
  if (!menuPermission.requiredPermissionCode) return canAccessByRole(activeItem, role);
  return permissionCodes.includes(menuPermission.requiredPermissionCode);
}

export default function AdminLayout({ children, title }: AdminLayoutProps) {
  const router = useRouter();
  const pathname = usePathname();
  const [status, setStatus] = useState<GuardStatus>('checking');
  const [fallbackMode, setFallbackMode] = useState(false);

  useEffect(() => {
    let mounted = true;

    const checkAccess = async () => {
      const currentQuery = window.location.search.replace(/^\?/, '');
      const token = getAccessToken();
      if (!token) {
        sessionStorage.setItem('authMessage', '관리자 화면은 로그인이 필요합니다.');
        router.replace(`/login?next=${encodeURIComponent(`${pathname}${currentQuery ? `?${currentQuery}` : ''}`)}`);
        return;
      }

      const role = getUserRole();
      if (!isAdminRole(role)) {
        if (mounted) setStatus('forbidden');
        return;
      }

      try {
        const [effective, menus] = await Promise.all([
          permissionGroupService.getMyEffectivePermissions(),
          permissionGroupService.getMenuPermissions(),
        ]);
        if (!mounted) return;
        const allowed = canAccessByPermission(role, pathname, currentQuery, effective.permissionCodes, menus);
        setFallbackMode(false);
        setStatus(allowed ? 'allowed' : 'forbidden');
      } catch {
        if (!mounted) return;
        const activeItem = findActiveAdminMenuItem(pathname, currentQuery);
        setFallbackMode(true);
        setStatus(!activeItem || canAccessByRole(activeItem, role) ? 'allowed' : 'forbidden');
      }
    };

    queueMicrotask(() => setStatus('checking'));
    checkAccess();

    return () => {
      mounted = false;
    };
  }, [pathname, router]);

  if (status === 'checking') {
    return (
      <div className="min-h-screen bg-[#f4f5f9] flex items-center justify-center">
        <p className="text-sm text-[#6f7a8a]">관리자 권한을 확인하고 있습니다.</p>
      </div>
    );
  }

  if (status === 'forbidden') {
    return (
      <div className="min-h-screen bg-[#f4f5f9] flex items-center justify-center px-4">
        <div className="max-w-[440px] w-full border border-[#e1e4ea] bg-white p-6 text-center">
          <h1 className="text-base font-semibold text-[#1a1f2e]">접근 권한이 없습니다</h1>
          <p className="mt-2 text-sm text-[#6f7a8a]">
            이 관리자 화면을 볼 수 있는 권한이 없습니다. 필요한 경우 최고관리자에게 권한을 요청해주세요.
          </p>
          {fallbackMode && (
            <p className="mt-3 text-xs text-[#a16b00] bg-[#fff7e6] border border-[#ffe1a6] px-3 py-2">
              권한 매트릭스 조회에 실패해 기본 역할 기준으로 접근을 확인했습니다.
            </p>
          )}
          <button
            type="button"
            onClick={() => router.replace('/admin')}
            className="mt-5 h-10 px-4 bg-[#1a1f2e] text-white text-sm font-medium hover:bg-[#2b3347] transition-colors"
          >
            대시보드로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-[#f4f5f9]">
      <AdminSidebarV2 />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <AdminTopbar title={title} />
        <main className="flex-1 overflow-y-auto p-6">{children}</main>
      </div>
    </div>
  );
}
