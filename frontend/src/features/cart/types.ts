export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productImageUrl: string;
  price: number;
  originalPrice: number;
  discountRate: number;
  quantity: number;
  stockQuantity: number;
}

export interface Cart {
  items: CartItem[];
  totalPrice: number;
  totalOriginalPrice: number;
  totalDiscount: number;
  shippingFee: number;
  finalPrice: number;
}
