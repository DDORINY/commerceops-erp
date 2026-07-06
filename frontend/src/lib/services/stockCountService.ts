import { apiClient, type PageResponse } from '@/lib/api';

export type ApiStockCountStatus = 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface ApiStockCountItem {
  itemId: number;
  skuId: number;
  skuCode: string;
  barcode: string | null;
  productId: number;
  productName: string;
  systemQuantity: number;
  countedQuantity: number | null;
  differenceQuantity: number;
  memo: string | null;
}

export interface ApiStockCount {
  stockCountId: number;
  countNumber: string;
  warehouseId: number;
  warehouseName: string;
  status: ApiStockCountStatus;
  memo: string | null;
  startedAt: string | null;
  completedAt: string | null;
  createdAt: string;
  items: ApiStockCountItem[];
}

export interface StockCountItemInput {
  skuId: number;
  countedQuantity?: number;
  memo?: string;
}

export const stockCountService = {
  getStockCounts: (status?: ApiStockCountStatus | 'ALL', page = 0, size = 20) => {
    const qs = new URLSearchParams({ page: String(page), size: String(size) });
    if (status && status !== 'ALL') qs.set('status', status);
    return apiClient<PageResponse<ApiStockCount>>(`/admin/stock-counts?${qs.toString()}`);
  },

  getStockCount: (stockCountId: number) =>
    apiClient<ApiStockCount>(`/admin/stock-counts/${stockCountId}`),

  createStockCount: (warehouseId: number, memo?: string) =>
    apiClient<ApiStockCount>('/admin/stock-counts', {
      method: 'POST',
      body: JSON.stringify({ warehouseId, memo }),
    }),

  updateItems: (stockCountId: number, items: StockCountItemInput[]) =>
    apiClient<ApiStockCount>(`/admin/stock-counts/${stockCountId}/items`, {
      method: 'PATCH',
      body: JSON.stringify({ items }),
    }),

  start: (stockCountId: number) =>
    apiClient<ApiStockCount>(`/admin/stock-counts/${stockCountId}/start`, { method: 'PATCH' }),

  complete: (stockCountId: number) =>
    apiClient<ApiStockCount>(`/admin/stock-counts/${stockCountId}/complete`, { method: 'PATCH' }),

  cancel: (stockCountId: number) =>
    apiClient<ApiStockCount>(`/admin/stock-counts/${stockCountId}/cancel`, { method: 'PATCH' }),
};
