import { apiClient, PageResponse } from '@/lib/api';

export interface ApiAccountingEntry {
  entryId: number;
  type: string;
  amount: number;
  description: string;
  referenceId: string | null;
  createdAt: string;
}

export interface ApiAccountingSummary {
  totalSales: number;
  totalRefunds: number;
  totalInbound: number;
  netSales: number;
}

export const accountingService = {
  getSummary: () => apiClient<ApiAccountingSummary>('/admin/accounting/summary'),

  getEntries: (type?: string, page = 0, size = 20) => {
    const qs = new URLSearchParams();
    if (type && type !== 'ALL') qs.set('type', type);
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiAccountingEntry>>(`/admin/accounting/entries?${qs.toString()}`);
  },
};
