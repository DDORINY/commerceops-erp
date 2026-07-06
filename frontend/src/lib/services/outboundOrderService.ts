import { apiClient, type PageResponse } from '@/lib/api';

export type OutboundOrderStatus = 'REQUESTED' | 'PICKING' | 'PICKED' | 'SHIPPED' | 'CANCELLED';

export interface ApiOutboundOrderItem {
  id: number;
  orderItemId: number;
  skuId: number | null;
  skuCode: string | null;
  barcode: string | null;
  productId: number;
  productName: string;
  quantity: number;
  pickedQuantity: number;
  scannedQuantity: number;
}

export interface ApiOutboundOrder {
  id: number;
  outboundNumber: string;
  orderId: number;
  orderNumber: string;
  customerName: string;
  customerEmail: string;
  warehouseId: number;
  warehouseName: string;
  status: OutboundOrderStatus;
  requestedAt: string;
  pickedAt: string | null;
  shippedAt: string | null;
  memo: string | null;
  totalQuantity: number;
  pickedQuantity: number;
  scannedQuantity: number;
  items: ApiOutboundOrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface OutboundOrderSearchParams {
  status?: OutboundOrderStatus | 'ALL';
  warehouseId?: number;
  orderId?: number;
  keyword?: string;
  page?: number;
  size?: number;
}

export interface OutboundOrderSaveRequest {
  orderId?: number;
  warehouseId: number;
  memo?: string;
}

function buildQuery(params: OutboundOrderSearchParams = {}) {
  const qs = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 20),
  });
  if (params.status && params.status !== 'ALL') qs.set('status', params.status);
  if (params.warehouseId) qs.set('warehouseId', String(params.warehouseId));
  if (params.orderId) qs.set('orderId', String(params.orderId));
  if (params.keyword) qs.set('keyword', params.keyword);
  return qs.toString();
}

export const outboundOrderService = {
  getOutboundOrders: (params?: OutboundOrderSearchParams) =>
    apiClient<PageResponse<ApiOutboundOrder>>(`/admin/outbound-orders?${buildQuery(params)}`),

  getOutboundOrder: (outboundOrderId: number) =>
    apiClient<ApiOutboundOrder>(`/admin/outbound-orders/${outboundOrderId}`),

  createOutboundOrder: (data: OutboundOrderSaveRequest) =>
    apiClient<ApiOutboundOrder>('/admin/outbound-orders', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateOutboundOrder: (outboundOrderId: number, data: OutboundOrderSaveRequest) =>
    apiClient<ApiOutboundOrder>(`/admin/outbound-orders/${outboundOrderId}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  pickOutboundOrder: (outboundOrderId: number) =>
    apiClient<ApiOutboundOrder>(`/admin/outbound-orders/${outboundOrderId}/pick`, {
      method: 'PATCH',
    }),

  scanOutboundItem: (outboundOrderId: number, barcode: string, quantity = 1) =>
    apiClient<ApiOutboundOrder>(`/admin/outbound-orders/${outboundOrderId}/scan`, {
      method: 'POST',
      body: JSON.stringify({ barcode, quantity }),
    }),

  cancelOutboundOrder: (outboundOrderId: number) =>
    apiClient<ApiOutboundOrder>(`/admin/outbound-orders/${outboundOrderId}/cancel`, {
      method: 'PATCH',
    }),
};
