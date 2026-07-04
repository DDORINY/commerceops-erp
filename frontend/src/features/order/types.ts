export type OrderStatus =
  | 'PENDING'
  | 'PAID'
  | 'PREPARING'
  | 'SHIPPING'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'REFUNDED';

export type PaymentStatus = 'READY' | 'PAID' | 'FAILED' | 'CANCELLED' | 'REFUNDED';

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productImageUrl: string;
  price: number;
  quantity: number;
}

export interface Order {
  orderId: number;
  orderNumber: string;
  customerName: string;
  customerEmail: string;
  items: OrderItem[];
  totalPrice: number;
  shippingFee: number;
  finalPrice: number;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  receiverName: string;
  receiverPhone: string;
  address: string;
  addressDetail: string;
  zipCode: string;
  createdAt: string;
  updatedAt: string;
}

export interface CheckoutRequest {
  items: { productId: number; quantity: number }[];
  receiverName: string;
  receiverPhone: string;
  address: string;
  addressDetail: string;
  zipCode: string;
  paymentMethod: string;
}
