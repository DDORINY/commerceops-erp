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
  postalCode: string | null;
  address: string;
  detailAddress: string;
  extraAddress: string | null;
  deliveryRequest: string | null;
  totalPrice: number;
  status: string;
  paymentStatus: string;
  payment: {
    provider: string | null;
    status: string;
    method: string | null;
    amount: number | null;
    approvedAt: string | null;
    failureCode: string | null;
    failureMessage: string | null;
  } | null;
  items: ApiOrderItem[];
  createdAt: string;
}

export interface OrderCreateRequest {
  orderType: 'CART' | 'BUY_NOW';
  paymentMethod: string;
  cartItemIds?: number[];
  productId?: number;
  quantity?: number;
  selectedOptions?: Record<string,string>;
  savedAddressId?: number;
  shippingAddress?: { addressName:string;recipientName:string;phone:string;postalCode:string;roadAddress:string;detailAddress?:string|null;extraAddress?:string|null;deliveryRequest?:string|null;saveAddress:boolean;setAsDefault:boolean };
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
  paymentProvider: string | null;
  paymentMethod: string | null;
  approvedAmount: number | null;
  approvedAt: string | null;
  paymentFailure: string | null;
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
