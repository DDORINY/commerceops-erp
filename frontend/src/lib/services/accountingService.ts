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

export interface ApiOrderRevenueRecognition {
  orderId: number;
  orderNumber: string;
  orderStatus: string;
  paymentStatus: string;
  orderAmount: number;
  recognized: boolean;
  transactionId: number | null;
  transactionNumber: string | null;
  recognizedAmount: number | null;
  recognizedAt: string | null;
  message: string;
}

export interface ApiAccountingRecognition {
  transactionType: ApiAccountingTransactionType;
  referenceType: ApiAccountingReferenceType;
  referenceId: number;
  sourceId: number;
  sourceNumber: string;
  amount: number;
  recognized: boolean;
  transactionId: number | null;
  transactionNumber: string | null;
  occurredAt: string | null;
  message: string;
}

export interface ApiShippingCostEntry {
  id: number;
  shipmentId: number;
  orderId: number;
  orderNumber: string;
  carrierId: number | null;
  carrierName: string | null;
  shippingMethodId: number | null;
  shippingMethodName: string | null;
  costAmount: number;
  chargedAmount: number;
  marginAmount: number;
  settlementStatus: string;
  occurredAt: string;
  memo: string | null;
  createdAt: string;
  updatedAt: string;
}

export type ApiSettlementBatchStatus = 'DRAFT' | 'CONFIRMED' | 'CLOSED' | 'CANCELLED';

export interface ApiSettlementBatchItem {
  id: number;
  referenceType: ApiAccountingReferenceType;
  referenceId: number;
  itemType: 'SALES' | 'REFUND' | 'SHIPPING_REVENUE' | 'SHIPPING_COST' | 'RETURN_FEE' | 'ADJUSTMENT';
  amount: number;
  memo: string | null;
  status: 'INCLUDED' | 'EXCLUDED' | 'ADJUSTED';
  createdAt: string;
}

export interface ApiSettlementBatch {
  id: number;
  batchNumber: string;
  periodStart: string;
  periodEnd: string;
  status: ApiSettlementBatchStatus;
  totalSales: number;
  totalRefunds: number;
  totalShippingFee: number;
  totalShippingCost: number;
  netAmount: number;
  closedAt: string | null;
  closedById: number | null;
  closedByName: string | null;
  createdAt: string;
  updatedAt: string;
  items: ApiSettlementBatchItem[];
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

  recognizeOrderRevenue: (orderId: number) =>
    apiClient<ApiOrderRevenueRecognition>(`/admin/accounting/orders/${orderId}/recognize-revenue`, {
      method: 'POST',
    }),

  getOrderRevenue: (orderId: number) =>
    apiClient<ApiOrderRevenueRecognition>(`/admin/accounting/orders/${orderId}/revenue`),

  getRevenueEvents: (page = 0, size = 20) => {
    const qs = new URLSearchParams();
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiAccountingTransaction>>(`/admin/accounting/revenue-events?${qs.toString()}`);
  },

  recognizePaymentRefund: (paymentId: number) =>
    apiClient<ApiAccountingRecognition>(`/admin/accounting/payments/${paymentId}/recognize-refund`, {
      method: 'POST',
    }),

  recognizeReturnRefund: (returnId: number) =>
    apiClient<ApiAccountingRecognition>(`/admin/accounting/returns/${returnId}/recognize-refund`, {
      method: 'POST',
    }),

  recognizeReturnFee: (returnId: number) =>
    apiClient<ApiAccountingRecognition>(`/admin/accounting/returns/${returnId}/recognize-return-fee`, {
      method: 'POST',
    }),

  getReturnFeeAccounting: (returnId: number) =>
    apiClient<ApiAccountingRecognition>(`/admin/accounting/returns/${returnId}/return-fee`),

  getRefundEvents: (page = 0, size = 20) => {
    const qs = new URLSearchParams();
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiAccountingTransaction>>(`/admin/accounting/refund-events?${qs.toString()}`);
  },

  getReturnFeeEvents: (page = 0, size = 20) => {
    const qs = new URLSearchParams();
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiAccountingTransaction>>(`/admin/accounting/return-fees?${qs.toString()}`);
  },

  recognizeShippingCost: (shipmentId: number) =>
    apiClient<ApiAccountingRecognition>(`/admin/accounting/shipments/${shipmentId}/recognize-shipping-cost`, {
      method: 'POST',
    }),

  getShippingCost: (shipmentId: number) =>
    apiClient<ApiShippingCostEntry>(`/admin/accounting/shipments/${shipmentId}/shipping-cost`),

  getShippingCosts: (page = 0, size = 20) => {
    const qs = new URLSearchParams();
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiShippingCostEntry>>(`/admin/accounting/shipping-costs?${qs.toString()}`);
  },

  getShippingCostEvents: (page = 0, size = 20) => {
    const qs = new URLSearchParams();
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiAccountingTransaction>>(`/admin/accounting/shipping-cost-events?${qs.toString()}`);
  },

  getSettlementBatches: (params: { status?: ApiSettlementBatchStatus | 'ALL'; page?: number; size?: number } = {}) => {
    const qs = new URLSearchParams();
    if (params.status && params.status !== 'ALL') qs.set('status', params.status);
    qs.set('page', String(params.page ?? 0));
    qs.set('size', String(params.size ?? 20));
    return apiClient<PageResponse<ApiSettlementBatch>>(`/admin/accounting/settlements?${qs.toString()}`);
  },

  getSettlementBatch: (settlementId: number) =>
    apiClient<ApiSettlementBatch>(`/admin/accounting/settlements/${settlementId}`),

  createSettlementBatch: (periodStart: string, periodEnd: string) =>
    apiClient<ApiSettlementBatch>('/admin/accounting/settlements', {
      method: 'POST',
      body: JSON.stringify({ periodStart, periodEnd }),
    }),

  closeSettlementBatch: (settlementId: number) =>
    apiClient<ApiSettlementBatch>(`/admin/accounting/settlements/${settlementId}/close`, {
      method: 'PATCH',
    }),
};
