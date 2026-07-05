'use client';

import Link from 'next/link';
import Image from 'next/image';
import type { ProductListItem } from '@/features/product/types';
import { formatPrice } from '@/lib/format';

interface ProductCardProps {
  product: ProductListItem;
}

export default function ProductCard({ product }: ProductCardProps) {
  const isSoldOut = product.status === 'SOLD_OUT' || product.stockQuantity === 0;
  const imageSrc = product.imageUrl || 'https://placehold.co/600x750?text=Image';

  return (
    <Link href={`/products/${product.id}`} className="group block">
      {/* 이미지 */}
      <div className="relative w-full overflow-hidden bg-[#f7f7f7] aspect-[4/5]">
        <Image
          src={imageSrc}
          alt={product.name}
          fill
          className={[
            'object-cover transition-transform duration-500 group-hover:scale-105',
            isSoldOut ? 'opacity-60' : '',
          ].join(' ')}
          sizes="(max-width: 768px) 50vw, 25vw"
        />

        {/* 배지 */}
        <div className="absolute top-3 left-3 flex flex-col gap-1.5">
          {product.isNew && !isSoldOut && (
            <span className="bg-[#222] text-white text-[10px] font-medium px-2 py-0.5 tracking-widest">
              신상품
            </span>
          )}
          {product.isBest && !isSoldOut && (
            <span className="bg-[#f3a6b8] text-white text-[10px] font-medium px-2 py-0.5 tracking-widest">
              베스트
            </span>
          )}
          {product.discountRate > 0 && !isSoldOut && (
            <span className="bg-[#d94f4f] text-white text-[10px] font-medium px-2 py-0.5">
              -{product.discountRate}%
            </span>
          )}
          {isSoldOut && (
            <span className="bg-[#777] text-white text-[10px] font-medium px-2 py-0.5 tracking-widest">
              품절
            </span>
          )}
        </div>

        {/* 장바구니 버튼 (hover) */}
        {!isSoldOut && (
          <button
            onClick={(e) => {
              e.preventDefault();
              alert(`"${product.name}" 장바구니에 추가됐습니다.`);
            }}
            className="absolute bottom-0 left-0 right-0 bg-[#222]/90 text-white text-xs py-3 translate-y-full group-hover:translate-y-0 transition-transform duration-300 tracking-widest"
          >
            장바구니 담기
          </button>
        )}
      </div>

      {/* 상품 정보 */}
      <div className="mt-3 px-0.5">
        <p className="text-[11px] text-[#999] mb-1 tracking-wide">{product.categoryName}</p>
        <p className="text-sm text-[#222] leading-snug mb-2 line-clamp-2">{product.name}</p>
        <div className="flex items-baseline gap-2">
          <span className="text-base font-semibold text-[#222]">
            {formatPrice(product.price)}
          </span>
          {product.discountRate > 0 && (
            <span className="text-xs text-[#bbb] line-through">
              {formatPrice(product.originalPrice)}
            </span>
          )}
        </div>
      </div>
    </Link>
  );
}
