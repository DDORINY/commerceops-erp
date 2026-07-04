'use client';

import { useState, useEffect, useCallback } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import { wishlistService, type ApiWishlistItem } from '@/lib/services/wishlistService';
import { formatPrice, formatDateTime } from '@/lib/format';

export default function WishlistPage() {
  const [items, setItems] = useState<ApiWishlistItem[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchWishlist = useCallback(() => {
    wishlistService
      .getWishlist()
      .then(setItems)
      .catch(() => setItems([]))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchWishlist();
  }, [fetchWishlist]);

  const handleRemove = async (productId: number) => {
    try {
      await wishlistService.toggle(productId);
      setItems((prev) => prev.filter((item) => item.productId !== productId));
    } catch (err) {
      alert(err instanceof Error ? err.message : '찜 해제에 실패했습니다.');
    }
  };

  const STATUS_LABEL: Record<string, string> = {
    ON_SALE: '판매중',
    SOLD_OUT: '품절',
    DISCONTINUED: '판매종료',
  };

  return (
    <>
      <ShopHeader />
      <main className="max-w-[1200px] mx-auto px-4 py-10">
        <h1 className="text-xl font-bold text-[#222] mb-8">찜 목록</h1>

        {loading ? (
          <div className="text-center text-[#aaa] py-20">불러오는 중...</div>
        ) : items.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-[#bbb] text-sm mb-6">찜한 상품이 없습니다.</p>
            <Link
              href="/products"
              className="inline-block border border-[#222] text-[#222] text-sm px-6 py-2.5 hover:bg-[#222] hover:text-white transition-colors"
            >
              쇼핑하러 가기
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">
            {items.map((item) => (
              <div key={item.wishlistId} className="group relative">
                <button
                  onClick={() => handleRemove(item.productId)}
                  className="absolute top-2 right-2 z-10 w-8 h-8 flex items-center justify-center bg-white/80 text-[#e05252] text-base leading-none hover:bg-white transition-colors"
                  aria-label="찜 해제"
                >
                  ♥
                </button>
                <Link href={`/products/${item.productId}`}>
                  <div className="relative aspect-[3/4] bg-[#f7f7f7] overflow-hidden mb-3">
                    <Image
                      src={item.imageUrl ?? '/placeholder.jpg'}
                      alt={item.productName}
                      fill
                      className="object-cover group-hover:scale-105 transition-transform duration-300"
                      sizes="(max-width: 768px) 50vw, 25vw"
                    />
                    {item.status !== 'ON_SALE' && (
                      <div className="absolute inset-0 bg-white/60 flex items-center justify-center">
                        <span className="bg-[#777] text-white text-xs font-medium px-4 py-1 tracking-widest">
                          {STATUS_LABEL[item.status] ?? item.status}
                        </span>
                      </div>
                    )}
                  </div>
                  <p className="text-xs text-[#aaa] mb-1">{item.categoryName}</p>
                  <p className="text-sm text-[#222] font-medium leading-snug mb-1 line-clamp-2">
                    {item.productName}
                  </p>
                  <p className="text-sm font-bold text-[#222]">{formatPrice(item.price)}</p>
                  <p className="text-xs text-[#bbb] mt-1">{formatDateTime(item.likedAt)}</p>
                </Link>
              </div>
            ))}
          </div>
        )}
      </main>
      <ShopFooter />
    </>
  );
}
