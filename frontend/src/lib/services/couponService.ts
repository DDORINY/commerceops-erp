import { apiClient } from '@/lib/api';

export interface ApiCoupon {
  id: number;
  code: string;
  discountType: 'FIXED' | 'PERCENT';
  discountValue: number;
  minOrderAmount: number;
  maxUsage: number;
  usedCount: number;
  expiresAt: string;
  active: boolean;
  createdAt: string;
}

export interface CouponValidateResult {
  code: string;
  discountType: string;
  discountValue: number;
  discountAmount: number;
  minOrderAmount: number;
}

export interface CouponCreatePayload {
  code: string;
  discountType: 'FIXED' | 'PERCENT';
  discountValue: number;
  minOrderAmount: number;
  maxUsage: number;
  expiresAt: string;
}

export const couponService = {
  validate: (code: string, orderAmount: number) =>
    apiClient<CouponValidateResult>(`/coupons/validate?code=${encodeURIComponent(code)}&orderAmount=${orderAmount}`, {
      method: 'POST',
    }),

  getAdminCoupons: () =>
    apiClient<ApiCoupon[]>('/admin/coupons'),

  createCoupon: (data: CouponCreatePayload) =>
    apiClient<ApiCoupon>('/admin/coupons', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  deactivateCoupon: (couponId: number) =>
    apiClient<null>(`/admin/coupons/${couponId}`, { method: 'DELETE' }),
};
