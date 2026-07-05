'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { getUserRole } from '@/lib/auth';

const NAV_ITEMS: Array<{ label: string; href: string; adminOnly?: boolean }> = [
  { label: 'Dashboard', href: '/admin' },
  { label: 'Products', href: '/admin/products' },
  { label: 'Categories', href: '/admin/categories', adminOnly: true },
  { label: 'Orders', href: '/admin/orders' },
  { label: 'Inventory', href: '/admin/inventory' },
  { label: 'Shipments', href: '/admin/shipments' },
  { label: 'Warehouses', href: '/admin/warehouses' },
  { label: 'Returns', href: '/admin/returns' },
  { label: 'Inquiries', href: '/admin/inquiries' },
  { label: 'Reviews', href: '/admin/reviews', adminOnly: true },
  { label: 'Accounting', href: '/admin/accounting', adminOnly: true },
  { label: 'Customers', href: '/admin/customers', adminOnly: true },
  { label: 'Sales', href: '/admin/sales' },
  { label: 'Coupons', href: '/admin/coupons', adminOnly: true },
];

export default function AdminSidebarV2() {
  const pathname = usePathname();
  const [userRole, setUserRole] = useState<string | null>(null);

  useEffect(() => {
    queueMicrotask(() => setUserRole(getUserRole()));
  }, []);

  const isAdminRole = userRole === 'ADMIN' || userRole === 'SUPER_ADMIN';
  const visibleNavItems = NAV_ITEMS.filter((item) => !item.adminOnly || isAdminRole);

  const isActive = (href: string) => {
    if (href === '/admin') return pathname === '/admin';
    return pathname.startsWith(href);
  };

  return (
    <aside className="w-[240px] min-h-screen bg-[#1a1f2e] flex-shrink-0 flex flex-col">
      <div className="h-[60px] flex items-center px-6 border-b border-white/10">
        <span className="text-white font-bold text-base tracking-widest uppercase">
          CommerceOps
        </span>
        <span className="ml-2 text-[10px] text-[#f3a6b8] font-semibold tracking-widest bg-[#f3a6b8]/20 px-1.5 py-0.5">
          ERP
        </span>
      </div>

      {userRole && !isAdminRole && (
        <div className="px-6 py-2 border-b border-white/10">
          <span className="text-xs text-amber-400 font-semibold tracking-wide">MANAGER</span>
        </div>
      )}

      <nav className="flex-1 py-4">
        <ul>
          {visibleNavItems.map((item) => (
            <li key={item.href}>
              <Link
                href={item.href}
                className={[
                  'flex items-center gap-3 px-6 py-3 text-sm transition-colors',
                  isActive(item.href)
                    ? 'bg-white/10 text-white border-r-2 border-[#f3a6b8]'
                    : 'text-[#8a9bb5] hover:text-white hover:bg-white/5',
                ].join(' ')}
              >
                <span className="w-1.5 h-1.5 rounded-full bg-current opacity-70" />
                {item.label}
              </Link>
            </li>
          ))}
        </ul>
      </nav>

      <div className="px-6 py-4 border-t border-white/10">
        <Link
          href="/"
          className="flex items-center gap-2 text-xs text-[#8a9bb5] hover:text-white transition-colors"
        >
          Shop Home
        </Link>
      </div>
    </aside>
  );
}
