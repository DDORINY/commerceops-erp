export type ProductStatus = 'ON_SALE' | 'SOLD_OUT' | 'HIDDEN' | 'DELETED';

export type ProductCategory =
  | 'BEST'
  | 'NEW'
  | '원피스'
  | '블라우스'
  | '아우터'
  | '니트'
  | '티셔츠'
  | '스커트'
  | '팬츠'
  | 'SALE';

export interface Product {
  id: number;
  categoryId: number;
  categoryName: ProductCategory;
  name: string;
  description: string;
  price: number;
  originalPrice: number;
  discountRate: number;
  stockQuantity: number;
  imageUrl: string;
  status: ProductStatus;
  isNew: boolean;
  isBest: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductListItem {
  id: number;
  categoryId: number;
  categoryName: ProductCategory;
  name: string;
  price: number;
  originalPrice: number;
  discountRate: number;
  stockQuantity: number;
  imageUrl: string;
  status: ProductStatus;
  isNew: boolean;
  isBest: boolean;
}

export interface ProductFilter {
  category?: ProductCategory;
  status?: ProductStatus;
  keyword?: string;
  page?: number;
  size?: number;
}
