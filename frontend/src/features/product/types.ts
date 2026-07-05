export type ProductStatus = 'ON_SALE' | 'SOLD_OUT' | 'HIDDEN' | 'DELETED';
export type ProductSalesStatus = 'DRAFT' | 'ON_SALE' | 'PAUSED' | 'SOLD_OUT' | 'DISCONTINUED';
export type StockDisplayStatus = 'IN_STOCK' | 'LOW_STOCK' | 'SOLD_OUT';

export interface Product {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string;
  price: number;
  originalPrice: number;
  discountRate: number;
  stockQuantity: number;
  imageUrl: string;
  status: ProductStatus;
  salesStatus?: ProductSalesStatus;
  brand?: string | null;
  tags?: string | null;
  purchasable?: boolean;
  stockDisplayStatus?: StockDisplayStatus;
  stockDisplayText?: string;
  remainingStockQuantity?: number;
  isNew: boolean;
  isBest: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductListItem {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  price: number;
  originalPrice: number;
  discountRate: number;
  stockQuantity: number;
  imageUrl: string;
  status: ProductStatus;
  salesStatus?: ProductSalesStatus;
  brand?: string | null;
  tags?: string | null;
  purchasable?: boolean;
  stockDisplayStatus?: StockDisplayStatus;
  stockDisplayText?: string;
  remainingStockQuantity?: number;
  isNew: boolean;
  isBest: boolean;
}

export interface ProductFilter {
  categoryId?: number;
  status?: ProductStatus;
  keyword?: string;
  page?: number;
  size?: number;
}
