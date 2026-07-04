import { apiClient, type PageResponse } from '@/lib/api';

export interface ApiReview {
  reviewId: number;
  productId: number;
  productName: string;
  userName: string;
  orderItemId: number;
  rating: number;
  content: string | null;
  createdAt: string;
}

export const reviewService = {
  createReview: (orderId: number, orderItemId: number, rating: number, content: string) =>
    apiClient<ApiReview>(`/orders/${orderId}/items/${orderItemId}/reviews`, {
      method: 'POST',
      body: JSON.stringify({ rating, content }),
    }),

  getProductReviews: (productId: number, page = 0, size = 10) =>
    apiClient<PageResponse<ApiReview>>(`/products/${productId}/reviews?page=${page}&size=${size}`),

  getMyReviews: () => apiClient<ApiReview[]>('/my/reviews'),

  deleteReview: (reviewId: number) =>
    apiClient<null>(`/reviews/${reviewId}`, { method: 'DELETE' }),

  getAdminReviews: (rating?: number | 'ALL', keyword?: string, page = 0, size = 15) => {
    const qs = new URLSearchParams();
    if (rating && rating !== 'ALL') qs.set('rating', String(rating));
    if (keyword) qs.set('keyword', keyword);
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiReview>>(`/admin/reviews?${qs.toString()}`);
  },

  deleteAdminReview: (reviewId: number) =>
    apiClient<null>(`/admin/reviews/${reviewId}`, { method: 'DELETE' }),
};
