import { apiClient, PageResponse } from '@/lib/api';

export interface ApiUser {
  id: number;
  email: string;
  name: string;
  phone: string | null;
  role: string;
  status: string;
  createdAt: string;
  orderCount: number;
  totalOrderAmount: number;
}

export const userService = {
  getAdminUsers: (params: { keyword?: string; page?: number; size?: number } = {}) => {
    const qs = new URLSearchParams();
    if (params.keyword) qs.set('keyword', params.keyword);
    if (params.page !== undefined) qs.set('page', String(params.page));
    if (params.size !== undefined) qs.set('size', String(params.size));
    const query = qs.toString() ? `?${qs.toString()}` : '';
    return apiClient<PageResponse<ApiUser>>(`/admin/users${query}`);
  },

  changeAdminUserRole: (userId: number, role: string) =>
    apiClient<ApiUser>(`/admin/users/${userId}/role`, {
      method: 'PATCH',
      body: JSON.stringify({ role }),
    }),
};
