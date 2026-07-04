export type InventoryStatus = 'NORMAL' | 'LOW' | 'OUT_OF_STOCK';

export type InboundType = 'PURCHASE' | 'RETURN' | 'ADJUSTMENT';

export interface InventoryItem {
  id: number;
  productId: number;
  productName: string;
  categoryName: string;
  stockQuantity: number;
  safetyStock: number;
  status: InventoryStatus;
  lastUpdatedAt: string;
}

export interface InventoryLog {
  id: number;
  productId: number;
  productName: string;
  type: InboundType;
  quantity: number;
  note: string;
  createdAt: string;
}
