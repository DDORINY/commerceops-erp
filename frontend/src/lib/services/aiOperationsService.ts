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

export const aiOperationsService = {
  getOverview: () => apiClient<AiOperationsOverview>('/admin/ai/overview'),
  getHealth: () => apiClient<AiOperationsHealth>('/admin/ai/health'),
  getProductRecommendations: (limit = 10) =>
    apiClient<AiInsight[]>(`/admin/ai/recommendations/products?limit=${limit}`),
  getDemandForecasts: (limit = 10) =>
    apiClient<AiInsight[]>(`/admin/ai/forecasts/demand?limit=${limit}`),
};
