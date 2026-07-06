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
  trackingNumberSource: string | null;
  trackingNumberIssuedAt: string | null;
  shippedAt: string | null;
  deliveredAt: string | null;
  createdAt: string;
}

export interface ApiShipmentLabel {
  id: number;
  shipmentId: number;
  orderId: number;
  orderNumber: string;
  receiverName: string;
  trackingNumber: string;
  carrier: string;
  labelFormat: string;
  printCount: number;
  lastPrintedAt: string | null;
  createdBy: number | null;
  createdAt: string;
}

export interface ApiShipmentLabelPreview {
  labelId: number;
  labelFormat: string;
  trackingNumber: string;
  carrier: string;
  orderNumber: string;
  receiverName: string;
  receiverPhone: string;
  address: string;
  printCount: number;
  html: string;
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

  generateTrackingNumber: (shipmentId: number, carrier: string) =>
    apiClient<ApiShipment>(`/admin/shipments/${shipmentId}/tracking-number`, {
      method: 'POST',
      body: JSON.stringify({ carrier }),
    }),

  getShipmentLabels: (shipmentId: number) =>
    apiClient<ApiShipmentLabel[]>(`/admin/shipments/${shipmentId}/labels`),

  createShipmentLabel: (shipmentId: number, labelFormat = 'SHIPMENT_100X150') =>
    apiClient<ApiShipmentLabelPreview>(`/admin/shipments/${shipmentId}/labels`, {
      method: 'POST',
      body: JSON.stringify({ labelFormat }),
    }),

  markShipmentLabelPrinted: (labelId: number) =>
    apiClient<ApiShipmentLabelPreview>(`/admin/shipments/labels/${labelId}/print`, {
      method: 'POST',
    }),

  markDelivered: (shipmentId: number) =>
    apiClient<ApiShipment>(`/admin/shipments/${shipmentId}/deliver`, {
      method: 'PATCH',
    }),

  getOrderShipment: (orderId: number) =>
    apiClient<ApiShipment>(`/orders/${orderId}/shipment`),
};
