import type { Cart } from './types';

export const mockCart: Cart = {
  items: [
    {
      id: 1,
      productId: 1,
      productName: '플리츠 플로럴 미디 원피스',
      productImageUrl: 'https://placehold.co/120x150/f9f0f2/c4788a?text=Dress',
      price: 52000,
      originalPrice: 68000,
      discountRate: 24,
      quantity: 1,
      stockQuantity: 38,
    },
    {
      id: 2,
      productId: 7,
      productName: '와이드 슬랙스 팬츠',
      productImageUrl: 'https://placehold.co/120x150/f2f2f2/888888?text=Pants',
      price: 48000,
      originalPrice: 48000,
      discountRate: 0,
      quantity: 2,
      stockQuantity: 44,
    },
  ],
  totalPrice: 148000,
  totalOriginalPrice: 164000,
  totalDiscount: 16000,
  shippingFee: 0,
  finalPrice: 148000,
};
