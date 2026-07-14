'use client';

import Link from 'next/link';
import { usePathname, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { categoryService, flattenCategoryTree, type ApiCategoryNode } from '@/lib/services/categoryService';

const FALLBACK_CATEGORIES = [
  { id: 0, name: '전체 상품', href: '/products' },
];

export default function DynamicCategoryNav() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [categories, setCategories] = useState<ApiCategoryNode[]>([]);
  const [failed, setFailed] = useState(false);
  const currentCategory = searchParams.get('category');

  useEffect(() => {
    let mounted = true;
    categoryService
      .getNavigationCategories()
      .then((nodes) => {
        if (!mounted) return;
        setCategories(flattenCategoryTree(nodes));
        setFailed(false);
      })
      .catch(() => {
        if (!mounted) return;
        setCategories([]);
        setFailed(true);
      });

    return () => {
      mounted = false;
    };
  }, []);

  const navItems = failed || categories.length === 0
    ? FALLBACK_CATEGORIES
    : [
      { id: 0, name: '전체 상품', href: '/products' },
      ...categories.map((category) => ({
        id: category.id,
        name: category.name,
        href: `/products?category=${encodeURIComponent(category.name)}`,
      })),
    ];

  return (
    <nav className="border-t border-[#f0f0f0]">
      <div className="max-w-[1200px] mx-auto px-4">
        <ul className="flex items-center justify-start md:justify-center overflow-x-auto scrollbar-none">
          {navItems.map((cat) => {
            const isAll = cat.href === '/products';
            const isActive = pathname === '/products' && (isAll ? !currentCategory : currentCategory === cat.name);

            return (
              <li key={cat.id}>
                <Link
                  href={cat.href}
                  className={[
                    'block px-4 sm:px-5 py-3 text-sm tracking-wide whitespace-nowrap transition-colors',
                    isActive ? 'border-b-2 border-[#222] font-semibold text-[#222]' : 'text-[#444] hover:text-[#222]',
                  ].join(' ')}
                >
                  {cat.name}
                </Link>
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
}
