import { apiClient, type PageResponse } from '@/lib/api';

export interface ApiBarcodeSku {
  skuId: number;
  skuCode: string;
  barcode: string | null;
  skuName: string;
  productId: number;
  productName: string;
  productCode: string | null;
  stockQuantity: number;
  active: boolean;
  safetyStockQuantity: number;
}

export interface ApiBarcodeLabel {
  id: number;
  skuId: number;
  skuCode: string;
  barcode: string;
  productName: string;
  labelFormat: string;
  printCount: number;
  lastPrintedAt: string | null;
  createdBy: number | null;
  createdAt: string;
}

export interface ApiBarcodeLabelPreview {
  labelId: number;
  labelFormat: string;
  barcode: string;
  skuCode: string;
  skuName: string;
  productName: string;
  html: string;
}

export const barcodeService = {
  searchSkus: (keyword?: string, page = 0, size = 20) => {
    const qs = new URLSearchParams({ page: String(page), size: String(size) });
    if (keyword) qs.set('keyword', keyword);
    return apiClient<PageResponse<ApiBarcodeSku>>(`/admin/barcodes?${qs.toString()}`);
  },

  getByBarcode: (barcode: string) =>
    apiClient<ApiBarcodeSku>(`/admin/barcodes/${encodeURIComponent(barcode)}`),

  getLabels: (keyword?: string, page = 0, size = 10) => {
    const qs = new URLSearchParams({ page: String(page), size: String(size) });
    if (keyword) qs.set('keyword', keyword);
    return apiClient<PageResponse<ApiBarcodeLabel>>(`/admin/barcode-labels?${qs.toString()}`);
  },

  createLabel: (skuId: number, labelFormat = 'SKU_60X40') =>
    apiClient<ApiBarcodeLabelPreview>(`/admin/barcodes/${skuId}/labels`, {
      method: 'POST',
      body: JSON.stringify({ labelFormat }),
    }),

  markPrinted: (labelId: number) =>
    apiClient<ApiBarcodeLabelPreview>(`/admin/barcode-labels/${labelId}/print`, {
      method: 'POST',
    }),
};
