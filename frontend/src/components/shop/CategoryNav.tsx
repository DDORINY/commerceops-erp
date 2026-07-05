'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const CATEGORIES = [
  { label: '베스트', href: `/products?category=${encodeURIComponent('베스트')}` },
  { label: '신상품', href: `/products?category=${encodeURIComponent('신상품')}` },
  { label: '원피스', href: '/products?category=원피스' },
  { label: '블라우스', href: '/products?category=블라우스' },
  { label: '아우터', href: '/products?category=아우터' },
  { label: '니트', href: '/products?category=니트' },
  { label: '티셔츠', href: '/products?category=티셔츠' },
  { label: '스커트', href: '/products?category=스커트' },
  { label: '팬츠', href: '/products?category=팬츠' },
  { label: '세일', href: `/products?category=${encodeURIComponent('세일')}` },
];

export default function CategoryNav() {
  const pathname = usePathname();

  return (
    <nav className="border-t border-[#f0f0f0]">
      <div className="max-w-[1200px] mx-auto px-4">
        <ul className="flex items-center justify-center overflow-x-auto scrollbar-none">
          {CATEGORIES.map((cat) => {
            const isActive =
              pathname === '/products' &&
              typeof window !== 'undefined' &&
              new URLSearchParams(window.location.search).get('category') === cat.label;

            return (
              <li key={cat.label}>
                <Link
                  href={cat.href}
                  className={[
                    'block px-5 py-3.5 text-sm tracking-wide whitespace-nowrap transition-colors',
                    cat.label === '세일'
                      ? 'text-[#d94f4f] font-medium hover:text-[#c43a3a]'
                      : cat.label === '베스트' || cat.label === '신상품'
                      ? 'font-semibold text-[#222] hover:text-[#f3a6b8]'
                      : 'text-[#444] hover:text-[#222]',
                    isActive ? 'border-b-2 border-[#222] font-medium' : '',
                  ]
                    .filter(Boolean)
                    .join(' ')}
                >
                  {cat.label}
                </Link>
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
}
