'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import MainBanner from '@/components/shop/MainBanner';
import ProductGrid from '@/components/shop/ProductGrid';
import { productService, toProductListItem } from '@/lib/services/productService';
import { categoryService, flattenCategoryTree, type ApiCategoryNode } from '@/lib/services/categoryService';
import { formatPrice } from '@/lib/format';
import type { ProductListItem } from '@/features/product/types';

interface RecentProduct {
  id: number;
  name: string;
  price: number;
  imageUrl: string;
  categoryName: string;
}

export default function HomePage() {
  const [products, setProducts] = useState<ProductListItem[]>([]);
  const [categories, setCategories] = useState<ApiCategoryNode[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loadError, setLoadError] = useState('');
  const [recentProducts] = useState<RecentProduct[]>(() => {
    if (typeof window === 'undefined') return [];
    try {
      const raw = localStorage.getItem('recent_products');
      return raw ? (JSON.parse(raw) as RecentProduct[]) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    productService
      .getProducts({ size: 12 })
      .then((res) => {
        setProducts(res.content.map(toProductListItem));
        setTotalCount(res.totalElements);
        setLoadError('');
      })
      .catch(() => {
        setProducts([]);
        setTotalCount(0);
        setLoadError('상품 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
      });

    categoryService
      .getNavigationCategories()
      .then((nodes) => setCategories(flattenCategoryTree(nodes).slice(0, 8)))
      .catch(() => setCategories([]));
  }, []);

  const newProducts = products.slice(0, 4);
  const bestProducts = products.slice(4, 8);
  const saleProducts = products.filter((product) => product.discountRate > 0).slice(0, 4);

  return (
    <>
      <ShopHeader />

      <main>
        <MainBanner />

        <div className="max-w-[1200px] mx-auto px-4">
          {loadError && <div className="mt-8 border border-[#f0d6d6] bg-[#fff7f7] px-4 py-3 text-sm text-[#c43a3a]">{loadError}</div>}

          <section className="py-12 border-b border-[#f0f0f0]">
            {categories.length > 0 ? (
              <div className="grid grid-cols-4 md:grid-cols-8 gap-3">
                {categories.map((cat) => (
                  <Link key={cat.id} href={`/products?category=${encodeURIComponent(cat.name)}`} className="flex flex-col items-center gap-2 group">
                    <div className="w-14 h-14 bg-[#f7f7f7] rounded-full flex items-center justify-center group-hover:bg-[#f3a6b8]/20 transition-colors">
                      <span className="text-xs font-medium text-[#555] group-hover:text-[#d97b93] transition-colors tracking-wide text-center px-1 line-clamp-2">{cat.name}</span>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="text-center text-sm text-[#aaa]">표시할 카테고리가 없습니다.</div>
            )}
          </section>

          <section className="py-14">
            <div className="flex items-end justify-between mb-8">
              <div>
                <p className="text-xs tracking-[0.25em] text-[#999] mb-1 uppercase">NEW ARRIVALS</p>
                <h2 className="text-2xl font-bold text-[#222] tracking-tight">신상품</h2>
              </div>
              <Link href="/products" className="text-xs text-[#777] hover:text-[#222] underline underline-offset-2 transition-colors">전체보기</Link>
            </div>
            <ProductGrid products={newProducts} columns={4} />
          </section>

          <section className="py-14 border-t border-[#f0f0f0]">
            <div className="flex items-end justify-between mb-8">
              <div>
                <p className="text-xs tracking-[0.25em] text-[#999] mb-1 uppercase">BEST PRODUCTS</p>
                <h2 className="text-2xl font-bold text-[#222] tracking-tight">추천 상품</h2>
              </div>
              <Link href="/products" className="text-xs text-[#777] hover:text-[#222] underline underline-offset-2 transition-colors">전체보기</Link>
            </div>
            <ProductGrid products={bestProducts} columns={4} />
          </section>

          <section className="py-14 border-t border-[#f0f0f0]">
            <div className="flex items-end justify-between mb-8">
              <div>
                <p className="text-xs tracking-[0.25em] text-[#d94f4f] mb-1 uppercase font-semibold">SALE</p>
                <h2 className="text-2xl font-bold text-[#222] tracking-tight">할인 상품</h2>
              </div>
              <Link href="/products" className="text-xs text-[#d94f4f] hover:text-[#c43a3a] underline underline-offset-2 transition-colors">전체보기</Link>
            </div>
            <ProductGrid products={saleProducts.length > 0 ? saleProducts : products.slice(8, 12)} columns={4} />
          </section>

          {recentProducts.length > 0 && (
            <section className="py-14 border-t border-[#f0f0f0]">
              <div className="flex items-end justify-between mb-8">
                <div>
                  <p className="text-xs tracking-[0.25em] text-[#999] mb-1 uppercase">RECENTLY VIEWED</p>
                  <h2 className="text-2xl font-bold text-[#222] tracking-tight">최근 본 상품</h2>
                </div>
              </div>
              <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                {recentProducts.slice(0, 5).map((p) => (
                  <Link key={p.id} href={`/products/${p.id}`} className="group">
                    <div className="relative aspect-[3/4] bg-[#f7f7f7] overflow-hidden mb-3">
                      <Image src={p.imageUrl || 'https://placehold.co/600x750?text=No+Image'} alt={p.name} fill className="object-cover group-hover:scale-105 transition-transform duration-300" sizes="(max-width: 768px) 50vw, 20vw" />
                    </div>
                    <p className="text-xs text-[#aaa] mb-1">{p.categoryName}</p>
                    <p className="text-sm text-[#222] font-medium leading-snug mb-1 line-clamp-2">{p.name}</p>
                    <p className="text-sm font-bold text-[#222]">{formatPrice(p.price)}</p>
                  </Link>
                ))}
              </div>
            </section>
          )}

          <section className="py-14 border-t border-[#f0f0f0] text-center">
            <p className="text-sm text-[#999] mb-5">{totalCount > 0 ? `총 ${totalCount}개 상품이 준비되어 있습니다.` : '상품을 불러오는 중입니다.'}</p>
            <Link href="/products" className="inline-flex items-center gap-2 border border-[#222] text-[#222] text-sm font-medium px-12 py-3.5 hover:bg-[#222] hover:text-white transition-colors">
              전체 상품 보기
            </Link>
          </section>
        </div>
      </main>

      <ShopFooter />
    </>
  );
}
