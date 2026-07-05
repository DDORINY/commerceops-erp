import { apiClient, publicApiClient } from '@/lib/api';

export interface ApiCategoryNode {
  id: number;
  name: string;
  parentId: number | null;
  depth: number;
  sortOrder: number;
  active: boolean;
  visibleInNav: boolean;
  slug: string | null;
  children: ApiCategoryNode[];
}

export interface CategoryPayload {
  name: string;
  parentId?: number | null;
  sortOrder?: number;
  active?: boolean;
  visibleInNav?: boolean;
  slug?: string | null;
}

export interface ApiCategorySummary extends CategoryPayload {
  id: number;
  parentId: number | null;
  depth: number;
  sortOrder: number;
  active: boolean;
  visibleInNav: boolean;
  slug: string | null;
}

export const flattenCategoryTree = (nodes: ApiCategoryNode[]): ApiCategoryNode[] =>
  nodes.flatMap((node) => [node, ...flattenCategoryTree(node.children ?? [])]);

export const categoryService = {
  getNavigationCategories: () =>
    publicApiClient<ApiCategoryNode[]>('/categories/navigation'),

  getAdminCategoryTree: () =>
    apiClient<ApiCategoryNode[]>('/admin/categories/tree'),

  createCategory: (data: CategoryPayload) =>
    apiClient<ApiCategorySummary>('/admin/categories', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateCategory: (id: number, data: Partial<CategoryPayload>) =>
    apiClient<ApiCategorySummary>(`/admin/categories/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),
};
