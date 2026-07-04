import { apiClient } from '@/lib/api';

export interface PaymentResponse {
  paymentId: number;
  orderId: number;
  paymentMethod: string;
  paymentStatus: string;
  paidAmount: number;
  transactionId: string;
}

export const paymentService = {
  completePayment: (orderId: number, paymentMethod: string) =>
    apiClient<PaymentResponse>('/payments/mock/complete', {
      method: 'POST',
      body: JSON.stringify({ orderId, paymentMethod }),
    }),
};
