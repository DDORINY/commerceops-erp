import { apiClient, type PageResponse } from '@/lib/api';

export interface ApiCarrier {
  id: number;
  code: string;
  name: string;
  trackingUrlTemplate: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ApiShippingMethod {
  id: number;
  code: string;
  name: string;
  carrierId: number | null;
  carrierName: string | null;
  defaultFee: number;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ShippingSearchParams {
  keyword?: string;
  carrierId?: number;
  active?: boolean | 'ALL';
  page?: number;
  size?: number;
}

export interface CarrierSaveRequest {
  code: string;
  name: string;
  trackingUrlTemplate?: string;
  active?: boolean;
}

export interface ShippingMethodSaveRequest {
  code: string;
  name: string;
  carrierId?: number;
  defaultFee: number;
  description?: string;
  active?: boolean;
}

function buildQuery(params: ShippingSearchParams = {}) {
  const qs = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 20),
  });
  if (params.keyword) qs.set('keyword', params.keyword);
  if (params.carrierId) qs.set('carrierId', String(params.carrierId));
  if (params.active !== undefined && params.active !== 'ALL') qs.set('active', String(params.active));
  return qs.toString();
}

export const shippingSettingService = {
  getCarriers: (params?: ShippingSearchParams) =>
    apiClient<PageResponse<ApiCarrier>>(`/admin/carriers?${buildQuery(params)}`),

  createCarrier: (data: CarrierSaveRequest) =>
    apiClient<ApiCarrier>('/admin/carriers', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateCarrier: (carrierId: number, data: CarrierSaveRequest) =>
    apiClient<ApiCarrier>(`/admin/carriers/${carrierId}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  updateCarrierActive: (carrierId: number, active: boolean) =>
    apiClient<ApiCarrier>(`/admin/carriers/${carrierId}/active`, {
      method: 'PATCH',
      body: JSON.stringify({ active }),
    }),

  getShippingMethods: (params?: ShippingSearchParams) =>
    apiClient<PageResponse<ApiShippingMethod>>(`/admin/shipping-methods?${buildQuery(params)}`),

  createShippingMethod: (data: ShippingMethodSaveRequest) =>
    apiClient<ApiShippingMethod>('/admin/shipping-methods', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateShippingMethod: (shippingMethodId: number, data: ShippingMethodSaveRequest) =>
    apiClient<ApiShippingMethod>(`/admin/shipping-methods/${shippingMethodId}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  updateShippingMethodActive: (shippingMethodId: number, active: boolean) =>
    apiClient<ApiShippingMethod>(`/admin/shipping-methods/${shippingMethodId}/active`, {
      method: 'PATCH',
      body: JSON.stringify({ active }),
    }),
};
