import { apiClient } from '@/lib/api';

export type AiRiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface AiInsight {
  id: string;
  targetType: string;
  targetId: number | null;
  title: string;
  score: number;
  riskLevel: AiRiskLevel;
  reason: string;
  features: Record<string, string | number | boolean | null>;
  modelName: string;
  generatedAt: string;
}

export interface AiOperationsOverview {
  datasetStatus: string;
  modelStatus: string;
  enabledModules: string[];
  highlights: AiInsight[];
  generatedAt: string;
}

export interface AiOperationsHealth {
  available: boolean;
  status: string;
  message: string;
  checkedItems: string[];
  checkedAt: string;
}

export interface AiReport {
  id: string;
  title: string;
  summary: string;
  relatedModule: string;
  modelName: string;
  evidenceSources: string[];
  interpretationGuide: string[];
  generatedAt: string;
}

export type AiDatasetKey =
  | 'PRODUCTS'
  | 'ORDERS'
  | 'ORDER_DEMAND'
  | 'REVIEWS'
  | 'PRODUCT_REVIEWS'
  | 'INVENTORY_SHIPPING'
  | 'SHIPPING_LEADTIME'
  | 'ACCOUNTING_TRANSACTIONS'
  | 'SETTLEMENT_BATCHES'
  | 'ACCOUNTING_CONSISTENCY_ISSUES';

export interface AiDatasetCatalogItem {
  key: AiDatasetKey;
  label: string;
  description: string;
  fields: string[];
}

export interface AiDatasetExport {
  key: AiDatasetKey;
  label: string;
  exportedAt: string;
  privacyMasked: boolean;
  rowCount: number;
  fields: string[];
  rows: Array<Record<string, unknown>>;
}

export const aiOperationsService = {
  getOverview: () => apiClient<AiOperationsOverview>('/admin/ai/overview'),
  getHealth: () => apiClient<AiOperationsHealth>('/admin/ai/health'),
  getProductRecommendations: (limit = 10) =>
    apiClient<AiInsight[]>(`/admin/ai/recommendations/products?limit=${limit}`),
  getDemandForecasts: (limit = 10) =>
    apiClient<AiInsight[]>(`/admin/ai/forecasts/demand?limit=${limit}`),
  getReviewAnalyses: (limit = 10) =>
    apiClient<AiInsight[]>(`/admin/ai/reviews/analysis?limit=${limit}`),
  getOrderAnomalies: (limit = 10) =>
    apiClient<AiInsight[]>(`/admin/ai/anomalies/orders?limit=${limit}`),
  getInventoryRiskAlerts: (limit = 10) =>
    apiClient<AiInsight[]>(`/admin/ai/risks/inventory?limit=${limit}`),
  getSettlementRiskAlerts: (limit = 10) =>
    apiClient<AiInsight[]>(`/admin/ai/risks/settlement?limit=${limit}`),
  getReports: () => apiClient<AiReport[]>('/admin/ai/reports'),
  getDatasetCatalog: () => apiClient<AiDatasetCatalogItem[]>('/admin/ai/datasets'),
  exportDataset: (key: AiDatasetKey, limit = 100) =>
    apiClient<AiDatasetExport>(`/admin/ai/datasets/${key}/export?limit=${limit}`),
};
