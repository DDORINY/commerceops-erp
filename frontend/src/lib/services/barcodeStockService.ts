import { apiClient } from '@/lib/api';

export interface ApiBarcodeWarehouseStock {
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
}

export interface ApiBarcodeStock {
  skuId: number;
  skuCode: string;
  barcode: string;
  skuName: string;
  productId: number;
  productName: string;
  productCode: string | null;
  productStockQuantity: number;
  safetyStockQuantity: number;
  active: boolean;
  warehouseStocks: ApiBarcodeWarehouseStock[];
}

export interface ApiBarcodeStockChange {
  barcode: string;
  skuId: number;
  skuCode: string;
  productId: number;
  productName: string;
  warehouseId: number;
  warehouseName: string;
  quantity: number;
  beforeProductStock: number;
  afterProductStock: number;
  beforeWarehouseStock: number;
  afterWarehouseStock: number;
  type: 'INBOUND' | 'OUTBOUND';
}

export const barcodeStockService = {
  getStock: (barcode: string) =>
    apiClient<ApiBarcodeStock>(`/admin/barcodes/${encodeURIComponent(barcode)}/stock`),

  inbound: (barcode: string, warehouseId: number, quantity: number, memo?: string) =>
    apiClient<ApiBarcodeStockChange>(`/admin/barcodes/${encodeURIComponent(barcode)}/inbound`, {
      method: 'POST',
      body: JSON.stringify({ warehouseId, quantity, memo }),
    }),

  outbound: (barcode: string, warehouseId: number, quantity: number, memo?: string) =>
    apiClient<ApiBarcodeStockChange>(`/admin/barcodes/${encodeURIComponent(barcode)}/outbound`, {
      method: 'POST',
      body: JSON.stringify({ warehouseId, quantity, memo }),
    }),
};
