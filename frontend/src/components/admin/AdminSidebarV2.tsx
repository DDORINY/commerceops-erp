'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { usePathname, useSearchParams } from 'next/navigation';
import { getUserRole } from '@/lib/auth';

type AdminRole = 'SUPER_ADMIN' | 'ADMIN' | 'MANAGER';

type MenuItem = {
  label: string;
  href: string;
  roles?: AdminRole[];
  note?: string;
};

type MenuGroup = {
  label: string;
  items: MenuItem[];
};

const ALL_ROLES: AdminRole[] = ['SUPER_ADMIN', 'ADMIN', 'MANAGER'];
const ADMIN_ROLES: AdminRole[] = ['SUPER_ADMIN', 'ADMIN'];
const SUPER_ONLY: AdminRole[] = ['SUPER_ADMIN'];

const MENU_GROUPS: MenuGroup[] = [
  {
    label: '대시보드',
    items: [
      { label: '대시보드', href: '/admin', roles: ALL_ROLES },
      { label: '알림', href: '/admin/settings?section=notifications', roles: ADMIN_ROLES, note: '설정 진입점' },
    ],
  },
  {
    label: '쇼핑몰 관리',
    items: [
      { label: '카테고리 관리', href: '/admin/categories', roles: ADMIN_ROLES },
      { label: '상단 네비 관리', href: '/admin/categories?focus=navigation', roles: ADMIN_ROLES },
      { label: '배너 관리', href: '/admin/banners', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '상품 관리',
    items: [
      { label: '상품 관리', href: '/admin/products', roles: ALL_ROLES },
      { label: '상품 등록', href: '/admin/products/new', roles: ADMIN_ROLES },
      { label: '상품 상태 관리', href: '/admin/products?focus=status', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '주문/배송/CS 관리',
    items: [
      { label: '주문 관리', href: '/admin/orders', roles: ALL_ROLES },
      { label: '배송 관리', href: '/admin/shipments', roles: ALL_ROLES },
      { label: '반품 관리', href: '/admin/returns', roles: ALL_ROLES },
      { label: '환불 관리', href: '/admin/returns?focus=refunds', roles: ADMIN_ROLES },
      { label: '문의 관리', href: '/admin/inquiries', roles: ALL_ROLES },
      { label: '리뷰 관리', href: '/admin/reviews', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '재고/창고 관리',
    items: [
      { label: '재고 관리', href: '/admin/inventory', roles: ALL_ROLES },
      { label: '창고 관리', href: '/admin/warehouses', roles: ALL_ROLES },
      { label: '입고 관리', href: '/admin/warehouses?focus=inbound', roles: ADMIN_ROLES },
      { label: '재고 이동', href: '/admin/warehouses?focus=transfers', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '매출/회계 관리',
    items: [
      { label: '매출 통계', href: '/admin/sales', roles: ALL_ROLES },
      { label: '회계 관리', href: '/admin/accounting', roles: ADMIN_ROLES },
      { label: '결제 내역', href: '/admin/accounting?focus=payments', roles: ADMIN_ROLES },
      { label: '환불 내역', href: '/admin/accounting?focus=refunds', roles: ADMIN_ROLES },
      { label: '정산 관리', href: '/admin/accounting?focus=settlements', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '마케팅 관리',
    items: [
      { label: '쿠폰 관리', href: '/admin/coupons', roles: ADMIN_ROLES },
      { label: '이벤트 관리', href: '/admin/settings?section=events', roles: ADMIN_ROLES, note: '후속 구현' },
      { label: '프로모션 관리', href: '/admin/settings?section=promotions', roles: ADMIN_ROLES, note: '후속 구현' },
    ],
  },
  {
    label: '인사/권한 관리',
    items: [
      { label: '직원 관리', href: '/admin/settings/staff', roles: SUPER_ONLY },
      { label: '부서 관리', href: '/admin/settings?section=departments', roles: SUPER_ONLY, note: 'v0.4 예정' },
      { label: '직급 관리', href: '/admin/settings?section=positions', roles: SUPER_ONLY, note: 'v0.4 예정' },
      { label: '권한 그룹 관리', href: '/admin/settings/permission-groups', roles: SUPER_ONLY },
      { label: '역할/권한 설정', href: '/admin/settings/roles', roles: SUPER_ONLY },
      { label: '메뉴/기능 권한', href: '/admin/settings/menu-permissions', roles: SUPER_ONLY },
    ],
  },
  {
    label: '시스템 설정',
    items: [
      { label: '사업자 설정', href: '/admin/settings?section=company', roles: ADMIN_ROLES },
      { label: '약관 설정', href: '/admin/settings?section=terms', roles: ADMIN_ROLES },
      { label: '개인정보처리방침 설정', href: '/admin/settings?section=privacy', roles: ADMIN_ROLES },
      { label: '배송/반품 정책 설정', href: '/admin/settings?section=policies', roles: ADMIN_ROLES },
      { label: '관리자 작업 이력', href: '/admin/settings/audit-logs', roles: ADMIN_ROLES },
      { label: '환경 설정', href: '/admin/settings?section=environment', roles: SUPER_ONLY, note: '후속 구현' },
    ],
  },
];

function splitHref(href: string) {
  const [path, query = ''] = href.split('?');
  return { path, query };
}

function roleLabel(role: string | null) {
  if (role === 'SUPER_ADMIN') return '최고관리자';
  if (role === 'ADMIN') return '관리자';
  if (role === 'MANAGER') return '매니저';
  return '권한 확인 중';
}

export default function AdminSidebarV2() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [userRole, setUserRole] = useState<string | null>(null);
  const [openGroupLabel, setOpenGroupLabel] = useState<string | null>(null);

  useEffect(() => {
    queueMicrotask(() => setUserRole(getUserRole()));
  }, []);

  const visibleGroups = useMemo(() => {
    if (!userRole || !['SUPER_ADMIN', 'ADMIN', 'MANAGER'].includes(userRole)) return [];
    return MENU_GROUPS
      .map((group) => ({
        ...group,
        items: group.items.filter((item) => !item.roles || item.roles.includes(userRole as AdminRole)),
      }))
      .filter((group) => group.items.length > 0);
  }, [userRole]);

  const currentQuery = searchParams.toString();

  const isActive = useMemo(() => {
    return (href: string) => {
      const { path, query } = splitHref(href);
      if (query) {
        return pathname === path && currentQuery === query;
      }
      if (path === '/admin') return pathname === '/admin';
      return pathname === path || pathname.startsWith(`${path}/`);
    };
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
                      <li key={`${group.label}-${item.label}-${item.href}`}>
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
