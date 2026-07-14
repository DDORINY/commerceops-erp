import type { ProductListItem } from '@/features/product/types';
import ProductCard from './ProductCard';

interface ProductGridProps {
  products: ProductListItem[];
  columns?: 2 | 3 | 4;
}

const colClass: Record<2 | 3 | 4, string> = {
  2: 'grid-cols-2',
  3: 'grid-cols-2 md:grid-cols-3',
  4: 'grid-cols-2 md:grid-cols-3 lg:grid-cols-4',
};

export default function ProductGrid({ products, columns = 4 }: ProductGridProps) {
  if (products.length === 0) {
    return <div className="py-20 text-center text-[#aaa] text-sm">표시할 상품이 없습니다.</div>;
  }

  return (
    <div className={`grid ${colClass[columns]} gap-x-3 gap-y-7 sm:gap-x-5 sm:gap-y-10`}>
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
}
