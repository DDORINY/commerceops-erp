import { apiClient, type PageResponse } from '@/lib/api';

export type ApiAccountingEntryType = 'SALE' | 'REFUND' | 'INBOUND';

export interface ApiAccountingEntry {
  entryId: number;
  type: ApiAccountingEntryType;
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

export type ApiAccountingLedgerStatus = 'OPEN' | 'CLOSED' | 'CANCELLED';
export type ApiAccountingTransactionType =
  | 'SALES'
  | 'REFUND'
  | 'SHIPPING_REVENUE'
  | 'SHIPPING_COST'
  | 'RETURN_FEE'
  | 'ADJUSTMENT'
  | 'SETTLEMENT';
export type ApiAccountingTransactionDirection = 'DEBIT' | 'CREDIT' | 'INCOME' | 'EXPENSE';
export type ApiAccountingReferenceType =
  | 'ORDER'
  | 'PAYMENT'
  | 'REFUND'
  | 'RETURN'
  | 'SHIPMENT'
  | 'OUTBOUND_ORDER'
  | 'SHIPPING_METHOD'
  | 'SETTLEMENT_BATCH';

export interface ApiAccountingLedger {
  ledgerId: number;
  ledgerNumber: string;
  period: string;
  status: ApiAccountingLedgerStatus;
  closedAt: string | null;
  closedById: number | null;
  closedByName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApiAccountingTransaction {
  transactionId: number;
  ledgerId: number | null;
  ledgerNumber: string | null;
  transactionNumber: string;
  type: ApiAccountingTransactionType;
  direction: ApiAccountingTransactionDirection;
  amount: number;
  referenceType: ApiAccountingReferenceType;
  referenceId: number;
  occurredAt: string;
  memo: string | null;
  createdById: number | null;
  createdByName: string | null;
  createdAt: string;
  updatedAt: string;
}

export const accountingService = {
  getSummary: () => apiClient<ApiAccountingSummary>('/admin/accounting/summary'),

  getEntries: (type?: ApiAccountingEntryType | 'ALL', page = 0, size = 20) => {
    const qs = new URLSearchParams();
    if (type && type !== 'ALL') qs.set('type', type);
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiAccountingEntry>>(`/admin/accounting/entries?${qs.toString()}`);
  },

  getLedgers: (params: { status?: ApiAccountingLedgerStatus | 'ALL'; period?: string; page?: number; size?: number } = {}) => {
    const qs = new URLSearchParams();
    if (params.status && params.status !== 'ALL') qs.set('status', params.status);
    if (params.period) qs.set('period', params.period);
    qs.set('page', String(params.page ?? 0));
    qs.set('size', String(params.size ?? 10));
    return apiClient<PageResponse<ApiAccountingLedger>>(`/admin/accounting/ledgers?${qs.toString()}`);
  },

  getTransactions: (
    params: {
      ledgerId?: number;
      type?: ApiAccountingTransactionType | 'ALL';
      direction?: ApiAccountingTransactionDirection | 'ALL';
      referenceType?: ApiAccountingReferenceType | 'ALL';
      referenceId?: number;
      page?: number;
      size?: number;
    } = {},
  ) => {
    const qs = new URLSearchParams();
    if (params.ledgerId) qs.set('ledgerId', String(params.ledgerId));
    if (params.type && params.type !== 'ALL') qs.set('type', params.type);
    if (params.direction && params.direction !== 'ALL') qs.set('direction', params.direction);
    if (params.referenceType && params.referenceType !== 'ALL') qs.set('referenceType', params.referenceType);
    if (params.referenceId) qs.set('referenceId', String(params.referenceId));
    qs.set('page', String(params.page ?? 0));
    qs.set('size', String(params.size ?? 10));
    return apiClient<PageResponse<ApiAccountingTransaction>>(`/admin/accounting/transactions?${qs.toString()}`);
  },
};
