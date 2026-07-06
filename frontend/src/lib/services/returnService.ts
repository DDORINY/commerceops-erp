import { apiClient, PageResponse } from '@/lib/api';

export interface ApiReturn {
  returnId: number;
  orderId: number;
  orderNumber: string;
  userName: string;
  reason: string;
  reasonDetail: string | null;
  status: string;
  adminNote: string | null;
  createdAt: string;
  updatedAt: string;
}

export type ReturnShipmentStatus = 'NOT_REQUESTED' | 'COLLECTION_REQUESTED' | 'IN_TRANSIT' | 'RECEIVED' | 'CANCELLED';
export type ReturnShippingFeePayer = 'UNDECIDED' | 'CUSTOMER' | 'COMPANY';

export interface ApiReturnShipmentInfo {
  id: number | null;
  returnId: number;
  carrier: string | null;
  trackingNumber: string | null;
  status: ReturnShipmentStatus;
  shippingFee: number | null;
  feePayer: ReturnShippingFeePayer;
  memo: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface ReturnShipmentPayload {
  carrier?: string;
  trackingNumber?: string;
  status?: ReturnShipmentStatus;
  shippingFee?: number | null;
  feePayer?: ReturnShippingFeePayer;
  memo?: string;
}

export const returnService = {
  createReturn: (orderId: number, reason: string, reasonDetail?: string) =>
    apiClient<ApiReturn>(`/orders/${orderId}/returns`, {
      method: 'POST',
      body: JSON.stringify({ reason, reasonDetail }),
    }),

  getMyReturns: () => apiClient<ApiReturn[]>('/returns'),

  getAdminReturns: (status?: string, keyword?: string, page = 0, size = 15) => {
    const qs = new URLSearchParams();
    if (status && status !== 'ALL') qs.set('status', status);
    if (keyword) qs.set('keyword', keyword);
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiReturn>>(`/admin/returns?${qs.toString()}`);
  },

  approveReturn: (returnId: number, adminNote?: string) =>
    apiClient<ApiReturn>(`/admin/returns/${returnId}/approve`, {
      method: 'PATCH',
      body: JSON.stringify({ adminNote }),
    }),

  rejectReturn: (returnId: number, adminNote?: string) =>
    apiClient<ApiReturn>(`/admin/returns/${returnId}/reject`, {
      method: 'PATCH',
      body: JSON.stringify({ adminNote }),
    }),

  getReturnShipment: (returnId: number) =>
    apiClient<ApiReturnShipmentInfo>(`/admin/returns/${returnId}/shipment`),

  saveReturnShipment: (returnId: number, payload: ReturnShipmentPayload, exists: boolean) =>
    apiClient<ApiReturnShipmentInfo>(`/admin/returns/${returnId}/shipment`, {
      method: exists ? 'PATCH' : 'POST',
      body: JSON.stringify(payload),
    }),
};
