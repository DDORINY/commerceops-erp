import { apiClient } from '@/lib/api';

export interface ApiCartItem {
  cartId: number;
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  stockQuantity: number;
  imageUrl: string;
  subtotal: number;
  selectedOptions: string | null;
}

export interface ApiCart {
  items: ApiCartItem[];
  totalPrice: number;
}

export const cartService = {
  getCart: () => apiClient<ApiCart>('/cart'),

  addToCart: (productId: number, quantity: number, selectedOptions?: Record<string, string>) =>
    apiClient<{ cartId: number; productId: number; quantity: number }>(
      '/cart',
      { method: 'POST', body: JSON.stringify({ productId, quantity, selectedOptions }) }
    ),

  updateCartItem: (cartId: number, quantity: number) =>
    apiClient<{ cartId: number; quantity: number; subtotal: number }>(
      `/cart/${cartId}`,
      { method: 'PATCH', body: JSON.stringify({ quantity }) }
    ),

  removeFromCart: (cartId: number) =>
    apiClient<null>(`/cart/${cartId}`, { method: 'DELETE' }),
};
