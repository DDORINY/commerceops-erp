import { apiClient, type PageResponse } from '@/lib/api';

export interface ApiWarehouseLocation {
  locationId: number;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  code: string;
  name: string;
  zone: string | null;
  aisle: string | null;
  rack: string | null;
  cell: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ApiWarehouseLocationStock {
  stockId: number;
  locationId: number;
  locationCode: string;
  locationName: string;
  warehouseId: number;
  warehouseName: string;
  skuId: number;
  skuCode: string;
  barcode: string | null;
  productId: number;
  productName: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  updatedAt: string;
}

export interface WarehouseLocationSaveRequest {
  warehouseId?: number;
  code: string;
  name: string;
  zone?: string;
  aisle?: string;
  rack?: string;
  cell?: string;
}

export const warehouseLocationService = {
  getLocations: (params?: { warehouseId?: number; active?: boolean | 'ALL'; keyword?: string; page?: number; size?: number }) => {
    const qs = new URLSearchParams({
      page: String(params?.page ?? 0),
      size: String(params?.size ?? 20),
    });
    if (params?.warehouseId) qs.set('warehouseId', String(params.warehouseId));
    if (params?.active !== undefined && params.active !== 'ALL') qs.set('active', String(params.active));
    if (params?.keyword) qs.set('keyword', params.keyword);
    return apiClient<PageResponse<ApiWarehouseLocation>>(`/admin/warehouse-locations?${qs.toString()}`);
  },

  createLocation: (data: WarehouseLocationSaveRequest & { warehouseId: number }) =>
    apiClient<ApiWarehouseLocation>('/admin/warehouse-locations', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateLocation: (locationId: number, data: WarehouseLocationSaveRequest) =>
    apiClient<ApiWarehouseLocation>(`/admin/warehouse-locations/${locationId}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  updateActive: (locationId: number, active: boolean) =>
    apiClient<ApiWarehouseLocation>(`/admin/warehouse-locations/${locationId}/active`, {
      method: 'PATCH',
      body: JSON.stringify({ active }),
    }),

  getLocationStocks: (locationId: number, page = 0, size = 20) =>
    apiClient<PageResponse<ApiWarehouseLocationStock>>(`/admin/warehouse-locations/${locationId}/stocks?page=${page}&size=${size}`),
};
