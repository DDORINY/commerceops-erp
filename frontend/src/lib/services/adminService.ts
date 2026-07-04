import { apiClient, PageResponse } from '@/lib/api';

export interface ApiDashboardSummary {
  totalSales: number;
  todaySales: number;
  totalOrders: number;
  todayOrders: number;
  totalCustomers: number;
  totalProducts: number;
  soldOutProductCount: number;
  lowStockProductCount: number;
  pendingOrderCount: number;
  orderStatusCounts: Record<string, number>;
}

export interface ApiSalesData {
  date: string;
  salesAmount: number;
  orderCount: number;
}

export interface ApiLowStockProduct {
  productId: number;
  productName: string;
  stockQuantity: number;
  lowStockThreshold: number;
}

export interface ApiTopProduct {
  productId: number;
  productName: string;
  orderCount: number;
  salesAmount: number;
}

export interface ApiInventoryItem {
  productId: number;
  productName: string;
  stockQuantity: number;
  lowStockThreshold: number;
  status: string;
}

export const adminService = {
  getDashboardSummary: () =>
    apiClient<ApiDashboardSummary>('/admin/dashboard/summary'),

  getSales: (period = 'DAILY', startDate?: string, endDate?: string) => {
    const qs = new URLSearchParams({ period });
    if (startDate) qs.set('startDate', startDate);
    if (endDate) qs.set('endDate', endDate);
    return apiClient<ApiSalesData[]>(`/admin/dashboard/sales?${qs.toString()}`);
  },

  getLowStockProducts: (limit = 10) =>
    apiClient<ApiLowStockProduct[]>(`/admin/dashboard/low-stock?limit=${limit}`),

  getTopProducts: (limit = 10) =>
    apiClient<ApiTopProduct[]>(`/admin/dashboard/top-products?limit=${limit}`),

  getInventory: (params: {
    keyword?: string;
    status?: string;
    lowStockOnly?: boolean;
    page?: number;
    size?: number;
  } = {}) => {
    const qs = new URLSearchParams({
      lowStockOnly: String(params.lowStockOnly ?? false),
      page: String(params.page ?? 0),
      size: String(params.size ?? 20),
    });
    if (params.keyword) qs.set('keyword', params.keyword);
    if (params.status && params.status !== 'ALL') qs.set('status', params.status);
    return apiClient<PageResponse<ApiInventoryItem>>(`/admin/inventory?${qs.toString()}`);
  },

  inbound: (warehouseId: number, productId: number, quantity: number, memo?: string) =>
    apiClient<{ warehouseId: number; productId: number; quantity: number; beforeStock: number; afterStock: number; type: string }>(
      '/admin/inventory/inbound',
      { method: 'POST', body: JSON.stringify({ warehouseId, productId, quantity, memo }) }
    ),

  adjust: (warehouseId: number, productId: number, quantity: number, memo?: string) =>
    apiClient<{ warehouseId: number; productId: number; beforeStock: number; afterStock: number; type: string }>(
      '/admin/inventory/adjust',
      { method: 'POST', body: JSON.stringify({ warehouseId, productId, quantity, memo }) }
    ),
};
