import { apiClient, type PageResponse } from '@/lib/api';

export interface ApiSku {
  id: number;
  productId: number;
  productName: string;
  productCode: string | null;
  optionSignature: string | null;
  skuCode: string;
  barcode: string | null;
  name: string;
  safetyStockQuantity: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SkuSearchParams {
  keyword?: string;
  productId?: number;
  active?: boolean | 'ALL';
  hasBarcode?: boolean | 'ALL';
  page?: number;
  size?: number;
}

export interface SkuSaveRequest {
  productId?: number;
  optionSignature?: string;
  skuCode?: string;
  barcode?: string;
  name?: string;
  safetyStockQuantity?: number;
  active?: boolean;
}

function buildQuery(params: SkuSearchParams = {}) {
  const qs = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 20),
  });
  if (params.keyword) qs.set('keyword', params.keyword);
  if (params.productId) qs.set('productId', String(params.productId));
  if (params.active !== undefined && params.active !== 'ALL') qs.set('active', String(params.active));
  if (params.hasBarcode !== undefined && params.hasBarcode !== 'ALL') qs.set('hasBarcode', String(params.hasBarcode));
  return qs.toString();
}

export const skuService = {
  getSkus: (params?: SkuSearchParams) =>
    apiClient<PageResponse<ApiSku>>(`/admin/skus?${buildQuery(params)}`),

  getSku: (skuId: number) =>
    apiClient<ApiSku>(`/admin/skus/${skuId}`),

  getProductSkus: (productId: number) =>
    apiClient<ApiSku[]>(`/admin/products/${productId}/skus`),

  createSku: (data: SkuSaveRequest) =>
    apiClient<ApiSku>('/admin/skus', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateSku: (skuId: number, data: SkuSaveRequest) =>
    apiClient<ApiSku>(`/admin/skus/${skuId}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  updateActive: (skuId: number, active: boolean) =>
    apiClient<ApiSku>(`/admin/skus/${skuId}/active`, {
      method: 'PATCH',
      body: JSON.stringify({ active }),
    }),

  regenerateBarcode: (skuId: number) =>
    apiClient<ApiSku>(`/admin/skus/${skuId}/barcode/regenerate`, {
      method: 'POST',
    }),
};
