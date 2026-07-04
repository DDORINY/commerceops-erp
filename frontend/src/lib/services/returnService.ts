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
};
