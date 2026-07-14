import { apiClient } from '@/lib/api';

export interface TossPaymentPrepareResponse {
  orderId: number;
  paymentOrderId: string;
  orderName: string;
  amount: number;
  customerKey: string;
  customerName: string;
  customerEmail: string;
}

export interface TossPaymentConfirmResponse {
  paymentId: number;
  orderId: number;
  paymentOrderId: string;
  status: string;
  method: string | null;
  approvedAmount: number;
  approvedAt: string | null;
}

export const paymentService = {
  prepareToss: (orderId: number) =>
    apiClient<TossPaymentPrepareResponse>('/payments/toss/prepare', {
      method: 'POST', body: JSON.stringify({ orderId }),
    }),
  confirmToss: (paymentKey: string, orderId: string, amount: number) =>
    apiClient<TossPaymentConfirmResponse>('/payments/toss/confirm', {
      method: 'POST', body: JSON.stringify({ paymentKey, orderId, amount }),
    }),
};
