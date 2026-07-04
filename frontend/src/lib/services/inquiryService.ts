import { apiClient, type PageResponse } from '@/lib/api';

export type InquiryType = 'PRODUCT' | 'ORDER' | 'OTHER';
export type InquiryStatus = 'WAITING' | 'ANSWERED' | 'CLOSED';

export interface ApiInquiry {
  inquiryId: number;
  userName: string;
  productId: number | null;
  productName: string | null;
  type: InquiryType;
  subject: string;
  content: string;
  answer: string | null;
  status: InquiryStatus;
  createdAt: string;
  updatedAt: string;
}

export const inquiryService = {
  createInquiry: (type: InquiryType, subject: string, content: string) =>
    apiClient<ApiInquiry>('/inquiries', {
      method: 'POST',
      body: JSON.stringify({ type, subject, content }),
    }),

  createProductInquiry: (productId: number, type: InquiryType, subject: string, content: string) =>
    apiClient<ApiInquiry>(`/products/${productId}/inquiries`, {
      method: 'POST',
      body: JSON.stringify({ type, subject, content }),
    }),

  getMyInquiries: () => apiClient<ApiInquiry[]>('/my/inquiries'),

  getProductInquiries: (productId: number) =>
    apiClient<ApiInquiry[]>(`/products/${productId}/inquiries`),

  getAdminInquiries: (status?: InquiryStatus | 'ALL', keyword?: string, page = 0, size = 15) => {
    const qs = new URLSearchParams();
    if (status && status !== 'ALL') qs.set('status', status);
    if (keyword) qs.set('keyword', keyword);
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiInquiry>>(`/admin/inquiries?${qs.toString()}`);
  },

  answerInquiry: (inquiryId: number, answer: string) =>
    apiClient<ApiInquiry>(`/admin/inquiries/${inquiryId}/answer`, {
      method: 'PATCH',
      body: JSON.stringify({ answer }),
    }),

  closeInquiry: (inquiryId: number) =>
    apiClient<ApiInquiry>(`/admin/inquiries/${inquiryId}/close`, {
      method: 'PATCH',
    }),
};
