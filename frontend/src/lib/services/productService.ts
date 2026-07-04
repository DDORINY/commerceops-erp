import { apiClient, PageResponse } from '@/lib/api';
import type { ProductListItem } from '@/features/product/types';

export interface ProductOptionGroup {
  name: string;
  values: string[];
}

export interface ApiProductItem {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  price: number;
  stockQuantity: number;
  imageUrl: string;
  status: string;
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
  stockQuantity: number;
  imageUrl: string;
  status: string;
  options: ProductOptionGroup[];
  createdAt: string;
  updatedAt: string;
}

export interface ApiCategory {
  id: number;
  name: string;
}

export function toProductListItem(p: ApiProductItem): ProductListItem {
  return {
    id: Number(p.id),
    categoryId: Number(p.categoryId),
    categoryName: p.categoryName as ProductListItem['categoryName'],
    name: p.name,
    price: p.price,
    originalPrice: p.price,
    discountRate: 0,
    stockQuantity: p.stockQuantity,
    imageUrl: p.imageUrl,
    status: p.status as ProductListItem['status'],
    isNew: false,
    isBest: false,
  };
}

export interface ProductCreatePayload {
  categoryId: number;
  name: string;
  description?: string;
  price: number;
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
    return apiClient<PageResponse<ApiProductItem>>(`/products${query}`);
  },

  getProduct: (id: number) => apiClient<ApiProductDetail>(`/products/${id}`),

  getAdminProducts: (params: {
    status?: string;
    keyword?: string;
    page?: number;
    size?: number;
  } = {}) => {
    const qs = new URLSearchParams();
    if (params.status && params.status !== 'ALL') qs.set('status', params.status);
    if (params.keyword) qs.set('keyword', params.keyword);
    if (params.page !== undefined) qs.set('page', String(params.page));
    if (params.size !== undefined) qs.set('size', String(params.size));
    const query = qs.toString() ? `?${qs.toString()}` : '';
    return apiClient<PageResponse<ApiProductItem>>(`/admin/products${query}`);
  },

  getAdminProduct: (id: number) => apiClient<ApiProductDetail>(`/admin/products/${id}`),

  getCategories: () => apiClient<ApiCategory[]>('/categories'),

  createProduct: (data: ProductCreatePayload) =>
    apiClient<ApiProductDetail>('/admin/products', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateProduct: (id: number, data: ProductUpdatePayload) =>
    apiClient<ApiProductDetail>(`/admin/products/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  deleteProduct: (id: number) =>
    apiClient<null>(`/admin/products/${id}`, { method: 'DELETE' }),
};
