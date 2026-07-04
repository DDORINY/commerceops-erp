import { apiClient } from '@/lib/api';

export interface ApiWishlistItem {
  wishlistId: number;
  productId: number;
  productName: string;
  price: number;
  imageUrl: string | null;
  categoryName: string;
  status: string;
  likedAt: string;
}

export interface WishlistToggleResponse {
  productId: number;
  liked: boolean;
}

export const wishlistService = {
  toggle: (productId: number) =>
    apiClient<WishlistToggleResponse>(`/wishlist/${productId}`, { method: 'POST' }),

  getWishlist: () =>
    apiClient<ApiWishlistItem[]>('/wishlist'),

  getStatus: (productId: number) =>
    apiClient<{ productId: number; liked: boolean }>(`/wishlist/${productId}/status`),
};
