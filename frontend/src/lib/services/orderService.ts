import { apiClient, PageResponse } from '@/lib/api';

export interface ApiOrder {
  orderId: number;
  orderNumber: string;
  totalPrice: number;
  status: string;
  paymentStatus: string;
  createdAt: string;
}

export interface ApiOrderItem {
  orderItemId: number;
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  subtotal: number;
  selectedOptions: string | null;
}

export interface ApiOrderDetail {
  orderId: number;
  orderNumber: string;
  receiverName: string;
  receiverPhone: string;
  address: string;
  detailAddress: string;
  totalPrice: number;
  status: string;
  paymentStatus: string;
  items: ApiOrderItem[];
  createdAt: string;
}

export interface OrderCreateRequest {
  receiverName: string;
  receiverPhone: string;
  address: string;
  detailAddress?: string;
  paymentMethod: string;
  cartItemIds: number[];
  couponCode?: string;
}

export interface OrderCreateResponse {
  orderId: number;
  orderNumber: string;
  totalPrice: number;
  status: string;
  paymentStatus: string;
}

export interface ApiAdminOrder {
  orderId: number;
  orderNumber: string;
  userName: string;
  userEmail: string;
  receiverName: string;
  totalPrice: number;
  status: string;
  paymentStatus: string;
  itemCount: number;
  createdAt: string;
}

export const orderService = {
  createOrder: (data: OrderCreateRequest) =>
    apiClient<OrderCreateResponse>('/orders', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getOrders: () => apiClient<ApiOrder[]>('/orders'),

  getOrderDetail: (orderId: number) =>
    apiClient<ApiOrderDetail>(`/orders/${orderId}`),

  cancelOrder: (orderId: number) =>
    apiClient<{ orderId: number; status: string }>(`/orders/${orderId}/cancel`, {
      method: 'PATCH',
    }),

  getAdminOrders: (status?: string, keyword?: string, page = 0, size = 20) => {
    const qs = new URLSearchParams();
    if (status && status !== 'ALL') qs.set('status', status);
    if (keyword) qs.set('keyword', keyword);
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiAdminOrder>>(`/admin/orders?${qs.toString()}`);
  },

  updateOrderStatus: (orderId: number, status: string) =>
    apiClient<{ orderId: number; status: string }>(
      `/admin/orders/${orderId}/status`,
      { method: 'PATCH', body: JSON.stringify({ status }) }
    ),
};
