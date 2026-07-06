import { apiClient, type PageResponse } from '@/lib/api';

export type ProductionOrderStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface ApiProductionOrderItem {
  id: number;
  skuId: number;
  skuCode: string;
  barcode: string | null;
  productId: number;
  productName: string;
  productCode: string | null;
  plannedQuantity: number;
  completedQuantity: number;
}

export interface ApiProductionOrder {
  id: number;
  productionNumber: string;
  status: ProductionOrderStatus;
  warehouseId: number;
  warehouseName: string;
  plannedQuantity: number;
  completedQuantity: number;
  startedAt: string | null;
  completedAt: string | null;
  memo: string | null;
  createdBy: number | null;
  updatedBy: number | null;
  createdAt: string;
  updatedAt?: string;
  items?: ApiProductionOrderItem[];
}

export interface ApiProductionReceipt {
  id: number;
  productionOrderId: number;
  productionNumber: string;
  skuId: number;
  skuCode: string;
  barcode: string | null;
  productId: number;
  productName: string;
  warehouseId: number;
  warehouseName: string;
  quantity: number;
  inventoryLogId: number | null;
  createdBy: number | null;
  createdAt: string;
}

export interface ProductionOrderItemPayload {
  skuId: number;
  plannedQuantity: number;
}

export interface ProductionOrderSavePayload {
  warehouseId: number;
  memo?: string;
  items: ProductionOrderItemPayload[];
}

export interface ProductionCompletePayload {
  memo?: string;
  items: Array<{
    skuId: number;
    completedQuantity: number;
  }>;
}

export interface ProductionOrderSearchParams {
  status?: ProductionOrderStatus | 'ALL';
  warehouseId?: number;
  skuId?: number;
  keyword?: string;
  page?: number;
  size?: number;
}

function buildOrderQuery(params: ProductionOrderSearchParams = {}) {
  const qs = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 20),
  });
  if (params.status && params.status !== 'ALL') qs.set('status', params.status);
  if (params.warehouseId) qs.set('warehouseId', String(params.warehouseId));
  if (params.skuId) qs.set('skuId', String(params.skuId));
  if (params.keyword) qs.set('keyword', params.keyword);
  return qs.toString();
}

export const productionService = {
  getOrders: (params?: ProductionOrderSearchParams) =>
    apiClient<PageResponse<ApiProductionOrder>>(`/admin/production-orders?${buildOrderQuery(params)}`),

  getOrder: (orderId: number) =>
    apiClient<ApiProductionOrder>(`/admin/production-orders/${orderId}`),

  createOrder: (data: ProductionOrderSavePayload) =>
    apiClient<ApiProductionOrder>('/admin/production-orders', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateOrder: (orderId: number, data: ProductionOrderSavePayload) =>
    apiClient<ApiProductionOrder>(`/admin/production-orders/${orderId}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  startOrder: (orderId: number) =>
    apiClient<ApiProductionOrder>(`/admin/production-orders/${orderId}/start`, {
      method: 'PATCH',
      body: JSON.stringify({}),
    }),

  completeOrder: (orderId: number, data: ProductionCompletePayload) =>
    apiClient<ApiProductionOrder>(`/admin/production-orders/${orderId}/complete`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  cancelOrder: (orderId: number, memo?: string) =>
    apiClient<ApiProductionOrder>(`/admin/production-orders/${orderId}/cancel`, {
      method: 'PATCH',
      body: JSON.stringify({ memo }),
    }),

  getReceipts: (params?: { productionOrderId?: number; skuId?: number; warehouseId?: number; page?: number; size?: number }) => {
    const qs = new URLSearchParams({
      page: String(params?.page ?? 0),
      size: String(params?.size ?? 20),
    });
    if (params?.productionOrderId) qs.set('productionOrderId', String(params.productionOrderId));
    if (params?.skuId) qs.set('skuId', String(params.skuId));
    if (params?.warehouseId) qs.set('warehouseId', String(params.warehouseId));
    return apiClient<PageResponse<ApiProductionReceipt>>(`/admin/production-receipts?${qs.toString()}`);
  },
};
