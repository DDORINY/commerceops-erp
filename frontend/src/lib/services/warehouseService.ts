import { apiClient, PageResponse } from '@/lib/api';

export interface ApiWarehouse {
  warehouseId: number;
  code: string;
  name: string;
  address: string;
  active: boolean;
  createdAt: string;
}

export interface ApiWarehouseStock {
  stockId: number;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  productId: number;
  productName: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  totalProductStock: number;
}

export interface ApiStockTransfer {
  transferId: number;
  transferNumber: string;
  fromWarehouseId: number;
  fromWarehouseName: string;
  toWarehouseId: number;
  toWarehouseName: string;
  productId: number;
  productName: string;
  quantity: number;
  status: 'PENDING' | 'COMPLETED';
  requestedAt: string;
  completedAt: string | null;
}

export const warehouseService = {
  getWarehouses: () => apiClient<ApiWarehouse[]>('/admin/warehouses'),

  createWarehouse: (code: string, name: string, address: string) =>
    apiClient<ApiWarehouse>('/admin/warehouses', {
      method: 'POST',
      body: JSON.stringify({ code, name, address }),
    }),

  getStocks: (warehouseId?: number, keyword?: string, page = 0, size = 20) => {
    const qs = new URLSearchParams({ page: String(page), size: String(size) });
    if (warehouseId) qs.set('warehouseId', String(warehouseId));
    if (keyword) qs.set('keyword', keyword);
    return apiClient<PageResponse<ApiWarehouseStock>>(`/admin/warehouse-stocks?${qs.toString()}`);
  },

  allocateStock: (warehouseId: number, productId: number, quantity: number) =>
    apiClient<ApiWarehouseStock>('/admin/warehouse-stocks/allocate', {
      method: 'POST',
      body: JSON.stringify({ warehouseId, productId, quantity }),
    }),

  getTransfers: (status?: string, page = 0, size = 20) => {
    const qs = new URLSearchParams({ page: String(page), size: String(size) });
    if (status && status !== 'ALL') qs.set('status', status);
    return apiClient<PageResponse<ApiStockTransfer>>(`/admin/stock-transfers?${qs.toString()}`);
  },

  createTransfer: (fromWarehouseId: number, toWarehouseId: number, productId: number, quantity: number) =>
    apiClient<ApiStockTransfer>('/admin/stock-transfers', {
      method: 'POST',
      body: JSON.stringify({ fromWarehouseId, toWarehouseId, productId, quantity }),
    }),

  completeTransfer: (transferId: number) =>
    apiClient<ApiStockTransfer>(`/admin/stock-transfers/${transferId}/complete`, {
      method: 'PATCH',
    }),
};
