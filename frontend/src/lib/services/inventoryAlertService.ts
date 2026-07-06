import { apiClient, type PageResponse } from '@/lib/api';

export interface ApiInventoryAlertRule {
  ruleId: number;
  skuId: number;
  skuCode: string;
  barcode: string | null;
  productId: number;
  productName: string;
  warehouseId: number | null;
  warehouseName: string;
  thresholdQuantity: number;
  active: boolean;
  memo: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApiLowStockAlert {
  ruleId: number;
  skuId: number;
  skuCode: string;
  barcode: string | null;
  productId: number;
  productName: string;
  warehouseId: number | null;
  warehouseName: string;
  currentQuantity: number;
  thresholdQuantity: number;
  shortageQuantity: number;
  memo: string | null;
}

export interface InventoryAlertRuleSaveRequest {
  skuId: number;
  warehouseId?: number | null;
  thresholdQuantity: number;
  memo?: string;
}

export const inventoryAlertService = {
  getRules: (params?: { warehouseId?: number; active?: boolean | 'ALL'; keyword?: string; page?: number; size?: number }) => {
    const qs = new URLSearchParams({
      page: String(params?.page ?? 0),
      size: String(params?.size ?? 20),
    });
    if (params?.warehouseId) qs.set('warehouseId', String(params.warehouseId));
    if (params?.active !== undefined && params.active !== 'ALL') qs.set('active', String(params.active));
    if (params?.keyword) qs.set('keyword', params.keyword);
    return apiClient<PageResponse<ApiInventoryAlertRule>>(`/admin/inventory-alert-rules?${qs.toString()}`);
  },

  createRule: (data: InventoryAlertRuleSaveRequest) =>
    apiClient<ApiInventoryAlertRule>('/admin/inventory-alert-rules', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateRule: (ruleId: number, data: InventoryAlertRuleSaveRequest) =>
    apiClient<ApiInventoryAlertRule>(`/admin/inventory-alert-rules/${ruleId}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  updateActive: (ruleId: number, active: boolean) =>
    apiClient<ApiInventoryAlertRule>(`/admin/inventory-alert-rules/${ruleId}/active`, {
      method: 'PATCH',
      body: JSON.stringify({ active }),
    }),

  getLowStockAlerts: (warehouseId?: number) => {
    const qs = new URLSearchParams();
    if (warehouseId) qs.set('warehouseId', String(warehouseId));
    const suffix = qs.toString() ? `?${qs.toString()}` : '';
    return apiClient<ApiLowStockAlert[]>(`/admin/inventory-alerts/low-stock${suffix}`);
  },
};
