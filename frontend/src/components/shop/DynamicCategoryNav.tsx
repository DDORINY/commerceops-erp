'use client';

import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import { categoryService, flattenCategoryTree, type ApiCategoryNode } from '@/lib/services/categoryService';

const FALLBACK_CATEGORIES = [
  { id: 0, name: 'BEST', href: '/products?category=BEST' },
  { id: -1, name: 'NEW', href: '/products?category=NEW' },
  { id: -2, name: 'SALE', href: '/products?category=SALE' },
];

export default function DynamicCategoryNav() {
  const pathname = usePathname();
  const [categories, setCategories] = useState<ApiCategoryNode[]>([]);
  const [failed, setFailed] = useState(false);
  const [currentCategory, setCurrentCategory] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    queueMicrotask(() => {
      if (mounted) {
        setCurrentCategory(new URLSearchParams(window.location.search).get('category'));
      }
    });
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

  useEffect(() => {
    queueMicrotask(() => {
      setCurrentCategory(new URLSearchParams(window.location.search).get('category'));
    });
  }, [pathname]);

  const navItems = failed
    ? FALLBACK_CATEGORIES
    : categories.map((category) => ({
      id: category.id,
      name: category.name,
      href: `/products?category=${encodeURIComponent(category.name)}`,
    }));

  if (navItems.length === 0) {
    return null;
  }

  return (
    <nav className="border-t border-[#f0f0f0]">
      <div className="max-w-[1200px] mx-auto px-4">
        <ul className="flex items-center justify-center overflow-x-auto scrollbar-none">
          {navItems.map((cat) => {
            const isActive = pathname === '/products' && currentCategory === cat.name;
            const isSale = cat.name === 'SALE';
            const isEmphasis = cat.name === 'BEST' || cat.name === 'NEW';

            return (
              <li key={cat.id}>
                <a
                  href={cat.href}
                  className={[
                    'block px-5 py-3.5 text-sm tracking-wide whitespace-nowrap transition-colors',
                    isSale
                      ? 'text-[#d94f4f] font-medium hover:text-[#c43a3a]'
                      : isEmphasis
                        ? 'font-semibold text-[#222] hover:text-[#f3a6b8]'
                        : 'text-[#444] hover:text-[#222]',
                    isActive ? 'border-b-2 border-[#222] font-medium' : '',
                  ]
                    .filter(Boolean)
                    .join(' ')}
                >
                  {cat.name}
                </a>
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
}
