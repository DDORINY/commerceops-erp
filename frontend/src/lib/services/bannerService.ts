import { apiClient, publicApiClient } from '@/lib/api';

export type BannerPosition = 'MAIN_TOP' | 'MAIN_MIDDLE' | 'MAIN_BOTTOM';

export interface ApiMainBanner {
  id: number;
  title: string;
  subtitle: string | null;
  description: string | null;
  imageUrl: string | null;
  linkUrl: string | null;
  position: BannerPosition;
  sortOrder: number;
  active: boolean;
  startsAt: string | null;
  endsAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface MainBannerPayload {
  title: string;
  subtitle?: string | null;
  description?: string | null;
  imageUrl?: string | null;
  linkUrl?: string | null;
  position?: BannerPosition;
  sortOrder?: number;
  active?: boolean;
  startsAt?: string | null;
  endsAt?: string | null;
}

export const BANNER_POSITION_LABEL: Record<BannerPosition, string> = {
  MAIN_TOP: '메인 상단',
  MAIN_MIDDLE: '메인 중단',
  MAIN_BOTTOM: '메인 하단',
};

export const bannerService = {
  getBanners: () => publicApiClient<ApiMainBanner[]>('/banners'),

  getAdminBanners: () => apiClient<ApiMainBanner[]>('/admin/banners'),

  getAdminBanner: (id: number) => apiClient<ApiMainBanner>(`/admin/banners/${id}`),

  createBanner: (data: MainBannerPayload) =>
    apiClient<ApiMainBanner>('/admin/banners', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateBanner: (id: number, data: MainBannerPayload) =>
    apiClient<ApiMainBanner>(`/admin/banners/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  deactivateBanner: (id: number) =>
    apiClient<void>(`/admin/banners/${id}`, {
      method: 'DELETE',
    }),
};

