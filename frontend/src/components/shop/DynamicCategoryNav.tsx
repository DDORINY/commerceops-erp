'use client';

import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import { categoryService, flattenCategoryTree, type ApiCategoryNode } from '@/lib/services/categoryService';

const FALLBACK_CATEGORIES = [
  { id: 0, name: '베스트', href: `/products?category=${encodeURIComponent('베스트')}` },
  { id: -1, name: '신상품', href: `/products?category=${encodeURIComponent('신상품')}` },
  { id: -2, name: '세일', href: `/products?category=${encodeURIComponent('세일')}` },
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
            const isSale = cat.name === '세일';
            const isEmphasis = cat.name === '베스트' || cat.name === '신상품';

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
