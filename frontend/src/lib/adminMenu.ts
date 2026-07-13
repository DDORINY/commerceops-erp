export type AdminRole = 'SUPER_ADMIN' | 'ADMIN' | 'MANAGER';

export type AdminMenuItem = {
  menuKey: string;
  label: string;
  href: string;
  roles?: AdminRole[];
  note?: string;
};

export type AdminMenuGroup = {
  label: string;
  items: AdminMenuItem[];
};

export const ALL_ADMIN_ROLES: AdminRole[] = ['SUPER_ADMIN', 'ADMIN', 'MANAGER'];
export const ADMIN_ROLES: AdminRole[] = ['SUPER_ADMIN', 'ADMIN'];
export const SUPER_ONLY: AdminRole[] = ['SUPER_ADMIN'];

export const ADMIN_MENU_GROUPS: AdminMenuGroup[] = [
  {
    label: '대시보드',
    items: [
      { menuKey: 'dashboard', label: '대시보드', href: '/admin', roles: ALL_ADMIN_ROLES },
      { menuKey: 'notifications', label: '알림', href: '/admin/settings?section=notifications', roles: ADMIN_ROLES, note: '설정 진입' },
    ],
  },
  {
    label: 'AI 운영',
    items: [
      { menuKey: 'ai-overview', label: 'AI 운영 개요', href: '/admin/ai', roles: ALL_ADMIN_ROLES },
      { menuKey: 'ai-recommendations', label: 'AI 상품 추천', href: '/admin/ai/recommendations', roles: ALL_ADMIN_ROLES },
      { menuKey: 'ai-demand-forecast', label: 'AI 수요 예측', href: '/admin/ai/demand-forecast', roles: ALL_ADMIN_ROLES },
      { menuKey: 'ai-datasets', label: 'AI 데이터셋', href: '/admin/settings?section=ai-datasets', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '쇼핑몰 관리',
    items: [
      { menuKey: 'categories', label: '카테고리 관리', href: '/admin/categories', roles: ADMIN_ROLES },
      { menuKey: 'top-navigation', label: '상단 네비 관리', href: '/admin/categories?focus=navigation', roles: ADMIN_ROLES },
      { menuKey: 'banners', label: '배너 관리', href: '/admin/banners', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '상품 관리',
    items: [
      { menuKey: 'products', label: '상품 관리', href: '/admin/products', roles: ALL_ADMIN_ROLES },
      { menuKey: 'product-create', label: '상품 등록', href: '/admin/products/new', roles: ADMIN_ROLES },
      { menuKey: 'product-status', label: '상품 상태 관리', href: '/admin/products?focus=status', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '주문/배송/CS 관리',
    items: [
      { menuKey: 'orders', label: '주문 관리', href: '/admin/orders', roles: ALL_ADMIN_ROLES },
      { menuKey: 'shipments', label: '배송 관리', href: '/admin/shipments', roles: ALL_ADMIN_ROLES },
      { menuKey: 'outbound-orders', label: '출고 관리', href: '/admin/outbound-orders', roles: ALL_ADMIN_ROLES },
      { menuKey: 'carriers', label: '택배사 관리', href: '/admin/carriers', roles: ADMIN_ROLES },
      { menuKey: 'shipping-methods', label: '배송 방법 관리', href: '/admin/shipping-methods', roles: ADMIN_ROLES },
      { menuKey: 'returns', label: '반품 관리', href: '/admin/returns', roles: ALL_ADMIN_ROLES },
      { menuKey: 'refunds', label: '환불 관리', href: '/admin/returns?focus=refunds', roles: ADMIN_ROLES },
      { menuKey: 'inquiries', label: '문의 관리', href: '/admin/inquiries', roles: ALL_ADMIN_ROLES },
      { menuKey: 'reviews', label: '리뷰 관리', href: '/admin/reviews', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '재고/창고 관리',
    items: [
      { menuKey: 'inventory', label: '재고 관리', href: '/admin/inventory', roles: ALL_ADMIN_ROLES },
      { menuKey: 'skus', label: 'SKU/바코드 관리', href: '/admin/skus', roles: ALL_ADMIN_ROLES },
      { menuKey: 'barcodes', label: '바코드 라벨 관리', href: '/admin/barcodes', roles: ALL_ADMIN_ROLES },
      { menuKey: 'barcode-stock', label: '바코드 입출고', href: '/admin/barcode-stock', roles: ALL_ADMIN_ROLES },
      { menuKey: 'warehouses', label: '창고 관리', href: '/admin/warehouses', roles: ALL_ADMIN_ROLES },
      { menuKey: 'warehouse-locations', label: '창고 위치 관리', href: '/admin/warehouse-locations', roles: ALL_ADMIN_ROLES },
      { menuKey: 'inbound', label: '입고 관리', href: '/admin/warehouses?focus=inbound', roles: ADMIN_ROLES },
      { menuKey: 'production', label: '생산 입고 관리', href: '/admin/production', roles: ALL_ADMIN_ROLES },
      { menuKey: 'stock-counts', label: '재고 실사', href: '/admin/stock-counts', roles: ALL_ADMIN_ROLES },
      { menuKey: 'stock-transfers', label: '재고 이동', href: '/admin/warehouses?focus=transfers', roles: ADMIN_ROLES },
      { menuKey: 'inventory-alerts', label: '안전재고 알림', href: '/admin/inventory-alerts', roles: ALL_ADMIN_ROLES },
    ],
  },
  {
    label: '매출/회계 관리',
    items: [
      { menuKey: 'sales', label: '매출 통계', href: '/admin/sales', roles: ALL_ADMIN_ROLES },
      { menuKey: 'accounting', label: '회계 관리', href: '/admin/accounting', roles: ADMIN_ROLES },
      { menuKey: 'payments', label: '결제 내역', href: '/admin/accounting?focus=payments', roles: ADMIN_ROLES },
      { menuKey: 'refund-history', label: '환불 내역', href: '/admin/accounting?focus=refunds', roles: ADMIN_ROLES },
      { menuKey: 'settlements', label: '정산 관리', href: '/admin/accounting?focus=settlements', roles: ADMIN_ROLES },
    ],
  },
  {
    label: '마케팅 관리',
    items: [
      { menuKey: 'coupons', label: '쿠폰 관리', href: '/admin/coupons', roles: ADMIN_ROLES },
      { menuKey: 'events', label: '이벤트 관리', href: '/admin/settings?section=events', roles: ADMIN_ROLES, note: '후속 구현' },
      { menuKey: 'promotions', label: '프로모션 관리', href: '/admin/settings?section=promotions', roles: ADMIN_ROLES, note: '후속 구현' },
    ],
  },
  {
    label: '인사/권한 관리',
    items: [
      { menuKey: 'staff', label: '직원 관리', href: '/admin/settings/staff', roles: SUPER_ONLY },
      { menuKey: 'departments', label: '부서 관리', href: '/admin/settings?section=departments', roles: SUPER_ONLY, note: '후속 구현' },
      { menuKey: 'positions', label: '직급 관리', href: '/admin/settings?section=positions', roles: SUPER_ONLY, note: '후속 구현' },
      { menuKey: 'permission-groups', label: '권한 그룹 관리', href: '/admin/settings/permission-groups', roles: SUPER_ONLY },
      { menuKey: 'roles', label: '역할/권한 설정', href: '/admin/settings/roles', roles: SUPER_ONLY },
      { menuKey: 'menu-permissions', label: '메뉴/기능 권한', href: '/admin/settings/menu-permissions', roles: SUPER_ONLY },
    ],
  },
  {
    label: '시스템 설정',
    items: [
      { menuKey: 'settings-company', label: '사업자 설정', href: '/admin/settings?section=company', roles: ADMIN_ROLES },
      { menuKey: 'settings-terms', label: '약관 설정', href: '/admin/settings?section=terms', roles: ADMIN_ROLES },
      { menuKey: 'settings-privacy', label: '개인정보처리방침 설정', href: '/admin/settings?section=privacy', roles: ADMIN_ROLES },
      { menuKey: 'settings-policies', label: '배송/반품 정책 설정', href: '/admin/settings?section=policies', roles: ADMIN_ROLES },
      { menuKey: 'audit-logs', label: '관리자 작업 이력', href: '/admin/settings/audit-logs', roles: ADMIN_ROLES },
      { menuKey: 'settings-environment', label: '환경 설정', href: '/admin/settings?section=environment', roles: SUPER_ONLY, note: '후속 구현' },
    ],
  },
];

export function splitHref(href: string) {
  const [path, query = ''] = href.split('?');
  return { path, query };
}

export function isAdminRole(role: string | null): role is AdminRole {
  return role === 'SUPER_ADMIN' || role === 'ADMIN' || role === 'MANAGER';
}

export function isHrefActive(href: string, pathname: string, currentQuery: string) {
  const { path, query } = splitHref(href);
  if (query) {
    return pathname === path && currentQuery === query;
  }
  if (path === '/admin') return pathname === '/admin';
  return pathname === path || pathname.startsWith(`${path}/`);
}

export function findActiveAdminMenuItem(pathname: string, currentQuery: string) {
  const allItems = ADMIN_MENU_GROUPS.flatMap((group) => group.items);
  const matches = allItems.filter((item) => isHrefActive(item.href, pathname, currentQuery));
  return matches.sort((a, b) => b.href.length - a.href.length)[0] ?? null;
}

export function canAccessByRole(item: AdminMenuItem, role: AdminRole) {
  return !item.roles || item.roles.includes(role);
}
