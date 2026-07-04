import { apiClient, PageResponse } from '@/lib/api';

export interface ApiShipment {
  shipmentId: number;
  orderId: number;
  orderNumber: string;
  receiverName: string;
  receiverPhone: string;
  address: string;
  status: string;
  trackingNumber: string | null;
  carrier: string | null;
  shippedAt: string | null;
  deliveredAt: string | null;
  createdAt: string;
}

export const shipmentService = {
  getAdminShipments: (status?: string, keyword?: string, page = 0, size = 15) => {
    const qs = new URLSearchParams();
    if (status && status !== 'ALL') qs.set('status', status);
    if (keyword) qs.set('keyword', keyword);
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiShipment>>(`/admin/shipments?${qs.toString()}`);
  },

  updateTracking: (shipmentId: number, trackingNumber: string, carrier: string) =>
    apiClient<ApiShipment>(`/admin/shipments/${shipmentId}/tracking`, {
      method: 'PATCH',
      body: JSON.stringify({ trackingNumber, carrier }),
    }),

  markDelivered: (shipmentId: number) =>
    apiClient<ApiShipment>(`/admin/shipments/${shipmentId}/deliver`, {
      method: 'PATCH',
    }),

  getOrderShipment: (orderId: number) =>
    apiClient<ApiShipment>(`/orders/${orderId}/shipment`),
};
