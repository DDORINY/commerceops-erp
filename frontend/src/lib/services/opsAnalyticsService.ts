import { apiClient } from '@/lib/api';

export interface ApiOpsAccountingOverview {
  totalSales: number;
  totalRefunds: number;
  totalInboundAmount: number;
  netSales: number;
  entryCount: number;
}

export interface ApiOpsSalesOverview {
  totalOrders: number;
  paidOrders: number;
  cancelledOrders: number;
  refundedOrders: number;
  totalRevenue: number;
  averagePaidOrderAmount: number;
  orderStatusCounts: Record<string, number>;
}

export interface ApiOpsWarehouseOverview {
  totalWarehouses: number;
  activeWarehouses: number;
  inactiveWarehouses: number;
  totalStockQuantity: number;
  totalReservedQuantity: number;
  totalAvailableQuantity: number;
  reservationStatusCounts: Record<string, number>;
}

export interface ApiOpsAnalyticsOverview {
  accounting: ApiOpsAccountingOverview;
  sales: ApiOpsSalesOverview;
  warehouse: ApiOpsWarehouseOverview;
  notes: string;
}

export const opsAnalyticsService = {
  getOverview: () => apiClient<ApiOpsAnalyticsOverview>('/admin/ops-analytics/overview'),
};
