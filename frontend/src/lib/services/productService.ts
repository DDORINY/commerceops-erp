import { apiClient, publicApiClient, PageResponse } from '@/lib/api';
import type { ProductListItem } from '@/features/product/types';

export interface ProductOptionGroup {
  name: string;
  values: string[];
}

export type ProductDetailBlockType = 'HEADING' | 'TEXT' | 'IMAGE' | 'NOTICE' | 'SPEC_TABLE' | 'HTML';
export type ProductSalesStatus = 'DRAFT' | 'ON_SALE' | 'PAUSED' | 'SOLD_OUT' | 'DISCONTINUED';
export type ProductDisplayStatus = 'VISIBLE' | 'HIDDEN';
export type StockDisplayStatus = 'IN_STOCK' | 'LOW_STOCK' | 'SOLD_OUT';
export type SalePeriodStatus = 'ALL' | 'ACTIVE' | 'UPCOMING' | 'ENDED';

export interface ProductDetailBlock {
  id?: number;
  blockType: ProductDetailBlockType;
  title?: string | null;
  content?: string | null;
  imageUrl?: string | null;
  specJson?: string | null;
  sortOrder: number;
  visible: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ApiProductItem {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  price: number;
  productCode: string | null;
  brand: string | null;
  originalPrice: number | null;
  discountPrice: number | null;
  tags: string | null;
  stockQuantity: number;
  imageUrl: string | null;
  status: string;
  salesStatus: ProductSalesStatus;
  purchasable: boolean;
  stockDisplayStatus: StockDisplayStatus;
  stockDisplayText: string;
  remainingStockQuantity: number;
  options: ProductOptionGroup[];
  createdAt: string;
}

export interface ApiProductDetail {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string;
  price: number;
  productCode: string | null;
  brand: string | null;
  manufacturer: string | null;
  modelName: string | null;
  origin: string | null;
  originalPrice: number | null;
  discountPrice: number | null;
  searchKeywords: string | null;
  tags: string | null;
  saleStartAt: string | null;
  saleEndAt: string | null;
  deliveryInfo: string | null;
  seoTitle: string | null;
  seoDescription: string | null;
  seoKeywords: string | null;
  stockQuantity: number;
  imageUrl: string | null;
  status: string;
  salesStatus: ProductSalesStatus;
  purchasable: boolean;
  stockDisplayStatus: StockDisplayStatus;
  stockDisplayText: string;
  remainingStockQuantity: number;
  options: ProductOptionGroup[];
  detailBlocks: ProductDetailBlock[];
  createdAt: string;
  updatedAt: string;
}

export interface ApiAdminProductItem extends ApiProductItem {
  purchasePrice: number | null;
  marginRate: number;
  displayStatus: ProductDisplayStatus;
  safetyStockQuantity: number;
}

export interface ApiAdminProductDetail extends ApiProductDetail {
  purchasePrice: number | null;
  marginRate: number;
  displayStatus: ProductDisplayStatus;
  safetyStockQuantity: number;
  deletedAt: string | null;
}

export interface ProductStatusHistory {
  id: number;
  productId: number;
  changedByUserId: number | null;
  changedByEmail: string | null;
  previousSalesStatus: ProductSalesStatus | null;
  newSalesStatus: ProductSalesStatus | null;
  previousDisplayStatus: ProductDisplayStatus | null;
  newDisplayStatus: ProductDisplayStatus | null;
  reason: string | null;
  createdAt: string;
}

export interface ProductOperationNote {
  id: number;
  productId: number;
  writerUserId: number | null;
  writerEmail: string | null;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProductBulkStatusUpdateResponse {
  updatedCount: number;
  products: ApiAdminProductItem[];
}

export interface ApiCategory {
  id: number;
  name: string;
  parentId?: number | null;
  depth?: number;
  sortOrder?: number;
  active?: boolean;
  visibleInNav?: boolean;
  slug?: string | null;
}

export function toProductListItem(p: ApiProductItem): ProductListItem {
  return {
    id: Number(p.id),
    categoryId: Number(p.categoryId),
    categoryName: p.categoryName,
    name: p.name,
    price: p.price,
    originalPrice: p.originalPrice ?? p.price,
    discountRate: p.originalPrice && p.discountPrice
      ? Math.max(0, Math.round((p.discountPrice / p.originalPrice) * 100))
      : 0,
    stockQuantity: p.stockQuantity,
    imageUrl: p.imageUrl ?? 'https://placehold.co/600x750?text=No+Image',
    status: p.status as ProductListItem['status'],
    salesStatus: p.salesStatus,
    brand: p.brand,
    tags: p.tags,
    purchasable: p.purchasable,
    stockDisplayStatus: p.stockDisplayStatus,
    stockDisplayText: p.stockDisplayText,
    remainingStockQuantity: p.remainingStockQuantity,
    isNew: false,
    isBest: false,
  };
}

export interface ProductCreatePayload {
  categoryId: number;
  name: string;
  description?: string;
  price: number;
  productCode?: string;
  brand?: string;
  manufacturer?: string;
  modelName?: string;
  origin?: string;
  originalPrice?: number;
  discountPrice?: number;
  purchasePrice?: number;
  searchKeywords?: string;
  tags?: string;
  saleStartAt?: string;
  saleEndAt?: string;
  deliveryInfo?: string;
  seoTitle?: string;
  seoDescription?: string;
  seoKeywords?: string;
  salesStatus?: ProductSalesStatus;
  displayStatus?: ProductDisplayStatus;
  safetyStockQuantity?: number;
  stockQuantity: number;
  imageUrl?: string;
  status: string;
  options?: ProductOptionGroup[];
}

export interface ProductUpdatePayload {
  categoryId?: number;
  name?: string;
  description?: string;
  price?: number;
  productCode?: string;
  brand?: string;
  manufacturer?: string;
  modelName?: string;
  origin?: string;
  originalPrice?: number;
  discountPrice?: number;
  purchasePrice?: number;
  searchKeywords?: string;
  tags?: string;
  saleStartAt?: string;
  saleEndAt?: string;
  deliveryInfo?: string;
  seoTitle?: string;
  seoDescription?: string;
  seoKeywords?: string;
  salesStatus?: ProductSalesStatus;
  displayStatus?: ProductDisplayStatus;
  safetyStockQuantity?: number;
  stockQuantity?: number;
  imageUrl?: string;
  status?: string;
  options?: ProductOptionGroup[];
}

export const productService = {
  getProducts: (params: {
    categoryId?: number;
    keyword?: string;
    sort?: string;
    minPrice?: number;
    maxPrice?: number;
    inStock?: boolean;
    page?: number;
    size?: number;
  } = {}) => {
    const qs = new URLSearchParams();
    if (params.categoryId) qs.set('categoryId', String(params.categoryId));
    if (params.keyword) qs.set('keyword', params.keyword);
    if (params.sort) qs.set('sort', params.sort);
    if (params.minPrice !== undefined) qs.set('minPrice', String(params.minPrice));
    if (params.maxPrice !== undefined) qs.set('maxPrice', String(params.maxPrice));
    if (params.inStock) qs.set('inStock', 'true');
    if (params.page !== undefined) qs.set('page', String(params.page));
    if (params.size !== undefined) qs.set('size', String(params.size));
    const query = qs.toString() ? `?${qs.toString()}` : '';
    return publicApiClient<PageResponse<ApiProductItem>>(`/products${query}`);
  },

  getProduct: (id: number) => publicApiClient<ApiProductDetail>(`/products/${id}`),

  getAdminProducts: (params: {
    status?: string;
    salesStatus?: string;
    displayStatus?: string;
    categoryId?: number;
    stockStatus?: string;
    lowStockOnly?: boolean;
    salePeriodStatus?: SalePeriodStatus;
    keyword?: string;
    page?: number;
    size?: number;
  } = {}) => {
    const qs = new URLSearchParams();
    if (params.status && params.status !== 'ALL') qs.set('status', params.status);
    if (params.salesStatus && params.salesStatus !== 'ALL') qs.set('salesStatus', params.salesStatus);
    if (params.displayStatus && params.displayStatus !== 'ALL') qs.set('displayStatus', params.displayStatus);
    if (params.categoryId) qs.set('categoryId', String(params.categoryId));
    if (params.stockStatus && params.stockStatus !== 'ALL') qs.set('stockStatus', params.stockStatus);
    if (params.lowStockOnly) qs.set('lowStockOnly', 'true');
    if (params.salePeriodStatus && params.salePeriodStatus !== 'ALL') qs.set('salePeriodStatus', params.salePeriodStatus);
    if (params.keyword) qs.set('keyword', params.keyword);
    if (params.page !== undefined) qs.set('page', String(params.page));
    if (params.size !== undefined) qs.set('size', String(params.size));
    const query = qs.toString() ? `?${qs.toString()}` : '';
    return apiClient<PageResponse<ApiAdminProductItem>>(`/admin/products${query}`);
  },

  getAdminProduct: (id: number) => apiClient<ApiAdminProductDetail>(`/admin/products/${id}`),

  getAdminProductDetailBlocks: (id: number) =>
    apiClient<ProductDetailBlock[]>(`/admin/products/${id}/detail-blocks`),

  saveAdminProductDetailBlocks: (id: number, blocks: ProductDetailBlock[]) =>
    apiClient<ProductDetailBlock[]>(`/admin/products/${id}/detail-blocks`, {
      method: 'PUT',
      body: JSON.stringify(blocks.map((block, index) => ({
        blockType: block.blockType,
        title: block.title,
        content: block.content,
        imageUrl: block.imageUrl,
        specJson: block.specJson,
        sortOrder: index,
        visible: block.visible,
      }))),
    }),

  getCategories: () => publicApiClient<ApiCategory[]>('/categories'),

  createProduct: (data: ProductCreatePayload) =>
    apiClient<ApiAdminProductDetail>('/admin/products', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateProduct: (id: number, data: ProductUpdatePayload) =>
    apiClient<ApiAdminProductDetail>(`/admin/products/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  updateProductStatus: (
    id: number,
    data: {
      salesStatus?: ProductSalesStatus;
      displayStatus?: ProductDisplayStatus;
      safetyStockQuantity?: number;
      reason?: string;
    }
  ) =>
    apiClient<ApiAdminProductDetail>(`/admin/products/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  bulkUpdateProductStatus: (data: {
    productIds: number[];
    salesStatus?: ProductSalesStatus;
    displayStatus?: ProductDisplayStatus;
    reason?: string;
  }) =>
    apiClient<ProductBulkStatusUpdateResponse>('/admin/products/bulk-status', {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  getProductStatusHistory: (id: number, limit = 20) =>
    apiClient<ProductStatusHistory[]>(`/admin/products/${id}/status-history?limit=${limit}`),

  getProductOperationNotes: (id: number, limit = 20) =>
    apiClient<ProductOperationNote[]>(`/admin/products/${id}/operation-notes?limit=${limit}`),

  createProductOperationNote: (id: number, content: string) =>
    apiClient<ProductOperationNote>(`/admin/products/${id}/operation-notes`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),

  deleteProduct: (id: number) =>
    apiClient<null>(`/admin/products/${id}`, { method: 'DELETE' }),
};
