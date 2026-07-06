'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { usePathname, useSearchParams } from 'next/navigation';
import {
  ADMIN_MENU_GROUPS,
  type AdminMenuItem,
  type AdminRole,
  canAccessByRole,
  isAdminRole,
  isHrefActive,
} from '@/lib/adminMenu';
import { getUserRole } from '@/lib/auth';
import { permissionGroupService, type AdminMenuPermission } from '@/lib/services/permissionGroupService';

type PermissionLoadStatus = 'idle' | 'loading' | 'ready' | 'fallback';

function roleLabel(role: string | null) {
  if (role === 'SUPER_ADMIN') return '최고관리자';
  if (role === 'ADMIN') return '관리자';
  if (role === 'MANAGER') return '매니저';
  return '권한 확인 중';
}

function canShowByPermission(
  item: AdminMenuItem,
  role: AdminRole,
  menuPermission: AdminMenuPermission | undefined,
  permissionCodes: Set<string>,
  status: PermissionLoadStatus
) {
  if (status !== 'ready') return canAccessByRole(item, role);
  if (!menuPermission) return canAccessByRole(item, role);
  if (!menuPermission.visible) return false;
  if (role === 'SUPER_ADMIN') return true;
  if (!menuPermission.requiredPermissionCode) return canAccessByRole(item, role);
  return permissionCodes.has(menuPermission.requiredPermissionCode);
}

export default function AdminSidebarV2() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [userRole, setUserRole] = useState<string | null>(null);
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);
  const [menuPermissions, setMenuPermissions] = useState<AdminMenuPermission[]>([]);
  const [permissionStatus, setPermissionStatus] = useState<PermissionLoadStatus>('idle');
  const [openGroupLabel, setOpenGroupLabel] = useState<string | null>(null);

  useEffect(() => {
    queueMicrotask(() => setUserRole(getUserRole()));
  }, []);

  useEffect(() => {
    if (!isAdminRole(userRole)) {
      queueMicrotask(() => setPermissionStatus('fallback'));
      return;
    }

    let mounted = true;
    const loadPermissions = async () => {
      setPermissionStatus('loading');
      try {
        const [effective, menus] = await Promise.all([
          permissionGroupService.getMyEffectivePermissions(),
          permissionGroupService.getMenuPermissions(),
        ]);
        if (!mounted) return;
        setPermissionCodes(effective.permissionCodes);
        setMenuPermissions(menus);
        setPermissionStatus('ready');
      } catch {
        if (!mounted) return;
        setPermissionCodes([]);
        setMenuPermissions([]);
        setPermissionStatus('fallback');
      }
    };

    loadPermissions();
    return () => {
      mounted = false;
    };
  }, [userRole]);

  const currentQuery = searchParams.toString();

  const menuPermissionByKey = useMemo(() => {
    return new Map(menuPermissions.map((permission) => [permission.menuKey, permission]));
  }, [menuPermissions]);

  const permissionCodeSet = useMemo(() => new Set(permissionCodes), [permissionCodes]);

  const visibleGroups = useMemo(() => {
    if (!isAdminRole(userRole)) return [];

    return ADMIN_MENU_GROUPS
      .map((group) => ({
        ...group,
        items: group.items.filter((item) =>
          canShowByPermission(
            item,
            userRole,
            menuPermissionByKey.get(item.menuKey),
            permissionCodeSet,
            permissionStatus
          )
        ),
      }))
      .filter((group) => group.items.length > 0);
  }, [menuPermissionByKey, permissionCodeSet, permissionStatus, userRole]);

  const isActive = useMemo(() => {
    return (href: string) => isHrefActive(href, pathname, currentQuery);
  }, [currentQuery, pathname]);

  const activeGroupLabel = useMemo(() => {
    return visibleGroups.find((group) => group.items.some((item) => isActive(item.href)))?.label ?? null;
  }, [isActive, visibleGroups]);

  useEffect(() => {
    queueMicrotask(() => setOpenGroupLabel(activeGroupLabel));
  }, [activeGroupLabel]);

  const toggleGroup = (label: string) => {
    setOpenGroupLabel((current) => (current === label ? null : label));
  };

  return (
    <aside className="w-[260px] min-h-screen bg-[#1a1f2e] flex-shrink-0 flex flex-col">
      <div className="h-[60px] flex items-center px-6 border-b border-white/10">
        <span className="text-white font-bold text-base tracking-widest uppercase">CommerceOps</span>
        <span className="ml-2 text-[10px] text-[#f3a6b8] font-semibold tracking-widest bg-[#f3a6b8]/20 px-1.5 py-0.5">ERP</span>
      </div>

      <div className="px-6 py-3 border-b border-white/10">
        <span className="text-xs text-amber-300 font-semibold tracking-wide">{roleLabel(userRole)}</span>
        {permissionStatus === 'loading' && (
          <p className="mt-1 text-[11px] text-[#8a9bb5]">메뉴 권한 확인 중</p>
        )}
        {permissionStatus === 'fallback' && isAdminRole(userRole) && (
          <p className="mt-1 text-[11px] text-[#8a9bb5]">기본 역할 기준 메뉴</p>
        )}
      </div>

      <nav className="flex-1 overflow-y-auto py-3 scrollbar-thin scrollbar-thumb-white/10 scrollbar-track-transparent">
        <div className="space-y-1">
          {visibleGroups.map((group) => {
            const hasActive = group.items.some((item) => isActive(item.href));
            const isOpen = openGroupLabel === group.label;
            return (
              <section key={group.label} className="px-3">
                <button
                  type="button"
                  onClick={() => toggleGroup(group.label)}
                  className={[
                    'w-full flex items-center justify-between px-3 py-2 text-xs font-semibold tracking-wide transition-colors',
                    hasActive ? 'text-white' : 'text-[#8a9bb5] hover:text-white',
                  ].join(' ')}
                  aria-expanded={isOpen}
                >
                  <span>{group.label}</span>
                  <span className="text-[10px]">{isOpen ? '−' : '+'}</span>
                </button>

                {isOpen && (
                  <ul className="pb-2">
                    {group.items.map((item) => (
                      <li key={`${group.label}-${item.menuKey}-${item.href}`}>
                        <Link
                          href={item.href}
                          className={[
                            'flex items-center justify-between gap-2 rounded-sm px-3 py-2 text-sm transition-colors',
                            isActive(item.href)
                              ? 'bg-white/10 text-white border-r-2 border-[#f3a6b8]'
                              : 'text-[#a5b0c2] hover:text-white hover:bg-white/5',
                          ].join(' ')}
                        >
                          <span className="flex items-center gap-2 min-w-0">
                            <span className="w-1.5 h-1.5 rounded-full bg-current opacity-70 shrink-0" />
                            <span className="truncate">{item.label}</span>
                          </span>
                          {item.note && <span className="shrink-0 text-[10px] text-[#66738a]">{item.note}</span>}
                        </Link>
                      </li>
                    ))}
                  </ul>
                )}
              </section>
            );
          })}
        </div>
      </nav>

      <div className="px-6 py-4 border-t border-white/10">
        <Link href="/" className="flex items-center gap-2 text-xs text-[#8a9bb5] hover:text-white transition-colors">
          쇼핑몰 바로가기
        </Link>
      </div>
    </aside>
  );
}
