'use client';

import Link from 'next/link';
import Image from 'next/image';
import type { ProductListItem } from '@/features/product/types';
import { formatPrice, PRODUCT_SALES_STATUS_LABEL } from '@/lib/format';

interface ProductCardProps {
  product: ProductListItem;
}

const getStatusLabel = (product: ProductListItem) => {
  if (product.stockDisplayStatus === 'SOLD_OUT' || product.status === 'SOLD_OUT' || product.stockQuantity <= 0) {
    return '품절';
  }
  if (product.salesStatus && product.salesStatus !== 'ON_SALE') {
    return PRODUCT_SALES_STATUS_LABEL[product.salesStatus] ?? '구매 불가';
  }
  return product.stockDisplayText ?? '구매 가능';
};

const getTags = (tags?: string | null) =>
  (tags ?? '')
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean)
    .slice(0, 2);

export default function ProductCard({ product }: ProductCardProps) {
  const isPurchasable = product.purchasable ?? (product.status !== 'SOLD_OUT' && product.stockQuantity > 0);
  const statusLabel = getStatusLabel(product);
  const imageSrc = product.imageUrl || 'https://placehold.co/600x750?text=No+Image';
  const tags = getTags(product.tags);

  return (
    <Link href={`/products/${product.id}`} className="group block">
      <div className="relative w-full overflow-hidden bg-[#f7f7f7] aspect-[4/5]">
        <Image
          src={imageSrc}
          alt={product.name}
          fill
          className={[
            'object-cover transition-transform duration-500 group-hover:scale-105',
            !isPurchasable ? 'opacity-60' : '',
          ].join(' ')}
          sizes="(max-width: 768px) 50vw, 25vw"
        />

        <div className="absolute top-3 left-3 flex flex-col gap-1.5">
          {product.isNew && isPurchasable && (
            <span className="bg-[#222] text-white text-[10px] font-medium px-2 py-0.5 tracking-widest">신상품</span>
          )}
          {product.isBest && isPurchasable && (
            <span className="bg-[#f3a6b8] text-white text-[10px] font-medium px-2 py-0.5 tracking-widest">베스트</span>
          )}
          {product.discountRate > 0 && isPurchasable && (
            <span className="bg-[#d94f4f] text-white text-[10px] font-medium px-2 py-0.5">-{product.discountRate}%</span>
          )}
          {!isPurchasable && (
            <span className="bg-[#777] text-white text-[10px] font-medium px-2 py-0.5 tracking-widest">{statusLabel}</span>
          )}
          {isPurchasable && product.stockDisplayStatus === 'LOW_STOCK' && (
            <span className="bg-[#fff2d8] text-[#b06b00] text-[10px] font-medium px-2 py-0.5">품절 임박</span>
          )}
        </div>

        {isPurchasable && (
          <div className="absolute bottom-0 left-0 right-0 bg-[#222]/90 text-white text-xs py-3 translate-y-full group-hover:translate-y-0 transition-transform duration-300 tracking-widest text-center">
            자세히 보기
          </div>
        )}
      </div>

      <div className="mt-3 px-0.5">
        <p className="text-[11px] text-[#999] mb-1 tracking-wide">{product.brand || product.categoryName}</p>
        <p className="text-sm text-[#222] leading-snug mb-2 line-clamp-2">{product.name}</p>
        <div className="flex items-baseline gap-2">
          <span className="text-base font-semibold text-[#222]">{formatPrice(product.price)}</span>
          {product.discountRate > 0 && product.originalPrice > product.price && (
            <span className="text-xs text-[#bbb] line-through">{formatPrice(product.originalPrice)}</span>
          )}
        </div>
        <p className="mt-1 text-xs text-[#888]">{statusLabel}</p>
        {tags.length > 0 && (
          <div className="mt-2 flex flex-wrap gap-1">
            {tags.map((tag) => (
              <span key={tag} className="bg-[#f7f8fc] text-[#7b8494] text-[10px] px-2 py-0.5">#{tag}</span>
            ))}
          </div>
        )}
      </div>
    </Link>
  );
}
