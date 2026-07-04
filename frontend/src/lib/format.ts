export function downloadCsv(filename: string, headers: string[], rows: (string | number | null | undefined)[][]): void {
  const escape = (v: string | number | null | undefined) => `"${String(v ?? '').replace(/"/g, '""')}"`;
  const csv = [headers, ...rows].map((r) => r.map(escape).join(',')).join('\r\n');
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

export function formatPrice(price: number): string {
  return price.toLocaleString('ko-KR') + '원';
}

export function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

export function formatDateTime(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export const ORDER_STATUS_LABEL: Record<string, string> = {
  PENDING: '결제 대기',
  PAID: '결제 완료',
  PREPARING: '상품 준비중',
  SHIPPING: '배송중',
  COMPLETED: '배송 완료',
  CANCELLED: '주문 취소',
  REFUNDED: '환불 완료',
};

export const ORDER_STATUS_COLOR: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  PAID: 'bg-blue-100 text-blue-700',
  PREPARING: 'bg-indigo-100 text-indigo-700',
  SHIPPING: 'bg-purple-100 text-purple-700',
  COMPLETED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-gray-100 text-gray-500',
  REFUNDED: 'bg-red-100 text-red-600',
};

export const INVENTORY_STATUS_LABEL: Record<string, string> = {
  NORMAL: '정상',
  LOW: '부족',
  LOW_STOCK: '부족',
  OUT_OF_STOCK: '품절',
};

export const INVENTORY_STATUS_COLOR: Record<string, string> = {
  NORMAL: 'bg-green-100 text-green-700',
  LOW: 'bg-yellow-100 text-yellow-700',
  LOW_STOCK: 'bg-yellow-100 text-yellow-700',
  OUT_OF_STOCK: 'bg-red-100 text-red-600',
};

export const RETURN_STATUS_LABEL: Record<string, string> = {
  REQUESTED: '반품 요청',
  APPROVED: '승인 완료',
  REJECTED: '거절',
};

export const RETURN_STATUS_COLOR: Record<string, string> = {
  REQUESTED: 'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-600',
};

export const RETURN_REASON_LABEL: Record<string, string> = {
  CHANGE_OF_MIND: '단순 변심',
  DEFECTIVE: '불량/파손',
  WRONG_DELIVERY: '오배송',
};

export const INQUIRY_STATUS_LABEL: Record<string, string> = {
  WAITING: '답변 대기',
  ANSWERED: '답변 완료',
  CLOSED: '종료',
};

export const INQUIRY_STATUS_COLOR: Record<string, string> = {
  WAITING: 'bg-yellow-100 text-yellow-700',
  ANSWERED: 'bg-green-100 text-green-700',
  CLOSED: 'bg-gray-100 text-gray-500',
};

export const INQUIRY_TYPE_LABEL: Record<string, string> = {
  PRODUCT: '상품 문의',
  ORDER: '주문 문의',
  OTHER: '기타',
};

export const ACCOUNTING_TYPE_LABEL: Record<string, string> = {
  SALE: '매출',
  REFUND: '환불',
  INBOUND: '매입',
};

export const ACCOUNTING_TYPE_COLOR: Record<string, string> = {
  SALE: 'bg-blue-100 text-blue-700',
  REFUND: 'bg-red-100 text-red-600',
  INBOUND: 'bg-purple-100 text-purple-700',
};

export const SHIPMENT_STATUS_LABEL: Record<string, string> = {
  READY: '배송준비',
  IN_TRANSIT: '배송중',
  DELIVERED: '배송완료',
  CANCELLED: '배송취소',
};

export const SHIPMENT_STATUS_COLOR: Record<string, string> = {
  READY: 'bg-indigo-100 text-indigo-700',
  IN_TRANSIT: 'bg-purple-100 text-purple-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-gray-100 text-gray-600',
};

export const CARRIER_OPTIONS = [
  'CJ대한통운',
  '한진택배',
  '롯데택배',
  '우체국택배',
  'CVSnet 편의점택배',
  '로젠택배',
];

export const PRODUCT_STATUS_LABEL: Record<string, string> = {
  ON_SALE: '판매중',
  SOLD_OUT: '품절',
  HIDDEN: '숨김',
  DELETED: '삭제',
};

export const PRODUCT_STATUS_COLOR: Record<string, string> = {
  ON_SALE: 'bg-green-100 text-green-700',
  SOLD_OUT: 'bg-red-100 text-red-600',
  HIDDEN: 'bg-gray-100 text-gray-500',
  DELETED: 'bg-gray-200 text-gray-400',
};
