import { apiClient } from '@/lib/api';

export interface PaymentResponse {
  paymentId: number;
  orderId: number;
  paymentMethod: string;
  paymentStatus: string;
  paidAmount: number;
  transactionId: string;
  idempotencyKey: string | null;
}

export const paymentService = {
  approvePayment: (
    orderId: number,
    paymentMethod: string,
    idempotencyKey?: string,
    providerTransactionId?: string
  ) =>
    apiClient<PaymentResponse>('/payments/approve', {
      method: 'POST',
      body: JSON.stringify({
        orderId,
        paymentMethod,
        idempotencyKey,
        providerTransactionId,
      }),
    }),

  completePayment: (orderId: number, paymentMethod: string) =>
    apiClient<PaymentResponse>('/payments/mock/complete', {
      method: 'POST',
      body: JSON.stringify({ orderId, paymentMethod }),
    }),

  cancelPayment: (paymentId: number, reason?: string) =>
    apiClient<PaymentResponse>(`/payments/${paymentId}/cancel`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    }),
};
