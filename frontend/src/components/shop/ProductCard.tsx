'use client';

import Link from 'next/link';
import Image from 'next/image';
import type { ProductListItem } from '@/features/product/types';
import { formatPrice } from '@/lib/format';

interface ProductCardProps {
  product: ProductListItem;
}

export default function ProductCard({ product }: ProductCardProps) {
  const isPurchasable = product.purchasable ?? (product.status !== 'SOLD_OUT' && product.stockQuantity > 0);
  const isSoldOut = product.stockDisplayStatus === 'SOLD_OUT' || product.status === 'SOLD_OUT' || product.stockQuantity === 0;
  const stockLabel = product.stockDisplayText ?? (isSoldOut ? '품절' : '구매 가능');
  const imageSrc = product.imageUrl || 'https://placehold.co/600x750?text=Image';

  return (
    <Link href={`/products/${product.id}`} className="group block">
      {/* ?대?吏 */}
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

        {/* 諛곗? */}
        <div className="absolute top-3 left-3 flex flex-col gap-1.5">
          {product.isNew && !isSoldOut && (
            <span className="bg-[#222] text-white text-[10px] font-medium px-2 py-0.5 tracking-widest">
              ?좎긽??
            </span>
          )}
          {product.isBest && !isSoldOut && (
            <span className="bg-[#f3a6b8] text-white text-[10px] font-medium px-2 py-0.5 tracking-widest">
              踰좎뒪??
            </span>
          )}
          {product.discountRate > 0 && !isSoldOut && (
            <span className="bg-[#d94f4f] text-white text-[10px] font-medium px-2 py-0.5">
              -{product.discountRate}%
            </span>
          )}
          {(isSoldOut || !isPurchasable) && (
            <span className="bg-[#777] text-white text-[10px] font-medium px-2 py-0.5 tracking-widest">
              {stockLabel}
            </span>
          )}
        </div>

        {/* ?λ컮援щ땲 踰꾪듉 (hover) */}
        {isPurchasable && (
          <button
            onClick={(e) => {
              e.preventDefault();
              alert(`"${product.name}" ?λ컮援щ땲??異붽??먯뒿?덈떎.`);
            }}
            className="absolute bottom-0 left-0 right-0 bg-[#222]/90 text-white text-xs py-3 translate-y-full group-hover:translate-y-0 transition-transform duration-300 tracking-widest"
          >
            ?λ컮援щ땲 ?닿린
          </button>
        )}
      </div>

      {/* ?곹뭹 ?뺣낫 */}
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


