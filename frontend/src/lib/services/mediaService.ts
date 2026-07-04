import { apiFormClient } from '@/lib/api';

export interface MediaFileResponse {
  id: number;
  originalFilename: string;
  storedFilename: string;
  url: string;
  contentType: string;
  size: number;
  mediaType: string;
  createdAt: string;
}

export const mediaService = {
  uploadProductImage: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiFormClient<MediaFileResponse>('/admin/media/product-images', formData);
  },
};
