'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import Button from '@/components/common/Button';
import { cartService, type ApiCartItem } from '@/lib/services/cartService';
import { formatPrice } from '@/lib/format';
import { notifyCartChanged } from '@/contexts/CartContext';

export default function CartPage() {
  const [items, setItems] = useState<ApiCartItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  useEffect(() => {
    cartService
      .getCart()
      .then((cart) => {
        setItems(cart.items);
        setSelectedIds(cart.items.map((item) => item.cartId));
        setErrorMessage('');
      })
      .catch(() => {
        setItems([]);
        setErrorMessage('장바구니를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
      })
      .finally(() => setLoading(false));
  }, []);

  const totalPrice = items.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const shippingFee = totalPrice >= 50000 ? 0 : 3000;
  const finalPrice = totalPrice + shippingFee;
  const selectedItems = items.filter((item) => selectedIds.includes(item.cartId));

  const updateQuantity = async (cartId: number, qty: number) => {
    const clampedQty = Math.max(1, qty);
    try {
      setActionMessage('');
      await cartService.updateCartItem(cartId, clampedQty);
      setItems((prev) =>
        prev.map((item) =>
          item.cartId === cartId ? { ...item, quantity: clampedQty } : item
        )
      );
      notifyCartChanged();
    } catch (err) {
      setActionMessage(err instanceof Error ? err.message : '수량 변경에 실패했습니다.');
    }
  };

  const removeItem = async (cartId: number) => {
    try {
      setActionMessage('');
      await cartService.removeFromCart(cartId);
      setItems((prev) => prev.filter((item) => item.cartId !== cartId));
      notifyCartChanged();
    } catch (err) {
      setActionMessage(err instanceof Error ? err.message : '삭제에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <>
        <ShopHeader />
        <main className="max-w-[1200px] mx-auto px-4 py-20 text-center text-[#aaa] text-sm">
          장바구니를 불러오는 중...
        </main>
        <ShopFooter />
      </>
    );
  }

  return (
    <>
      <ShopHeader />

      <main className="max-w-[1200px] mx-auto px-4 py-10">
        <h1 className="text-xl font-bold text-[#222] mb-8 pb-4 border-b border-[#e5e5e5]">장바구니</h1>

        {actionMessage && (
          <div className="mb-5 border border-[#f0d6d6] bg-[#fff7f7] px-4 py-3 text-sm text-[#c43a3a]">
            {actionMessage}
          </div>
        )}

        {errorMessage ? (
          <div className="py-24 text-center">
            <p className="text-[#c43a3a] text-sm mb-6">{errorMessage}</p>
            <Link href="/products">
              <Button variant="outline">쇼핑 계속하기</Button>
            </Link>
          </div>
        ) : items.length === 0 ? (
          <div className="py-24 text-center">
            <p className="text-[#aaa] text-sm mb-6">장바구니가 비어 있습니다.</p>
            <Link href="/products">
              <Button variant="outline">쇼핑 계속하기</Button>
            </Link>
          </div>
        ) : (
          <div className="flex flex-col lg:flex-row gap-8">
            <div className="flex-1">
              <div className="flex items-center px-4 py-3 bg-[#f8f8f8] border-b border-[#e5e5e5] text-xs text-[#999]">
                <span className="flex-1">상품 정보</span>
                <span className="w-28 text-center">수량</span>
                <span className="w-28 text-right">금액</span>
                <span className="w-10" />
              </div>

              {items.map((item) => (
                <div key={item.cartId} className="flex items-center px-4 py-5 border-b border-[#f0f0f0]">
                  <input type="checkbox" checked={selectedIds.includes(item.cartId)} onChange={(e) => setSelectedIds((prev) => e.target.checked ? [...prev, item.cartId] : prev.filter((id) => id !== item.cartId))} className="mr-3" aria-label={`${item.productName} 선택`} />
                  <div className="relative w-20 h-24 bg-[#f7f7f7] mr-4 flex-shrink-0">
                    <Image
                      src={item.imageUrl}
                      alt={item.productName}
                      fill
                      className="object-cover"
                      sizes="80px"
                    />
                  </div>

                  <div className="flex-1 min-w-0">
                    <Link
                      href={`/products/${item.productId}`}
                      className="text-sm text-[#222] font-medium hover:text-[#d97b93] transition-colors line-clamp-2"
                    >
                      {item.productName}
                    </Link>
                    <p className="text-sm font-semibold text-[#222] mt-0.5">
                      {formatPrice(item.price)}
                    </p>
                  </div>

                  <div className="w-28 flex justify-center">
                    <div className="flex items-center border border-[#ddd]">
                      <button
                        onClick={() => updateQuantity(item.cartId, item.quantity - 1)}
                        className="w-8 h-8 flex items-center justify-center text-[#555] hover:bg-[#f5f5f5]"
                      >
                        −
                      </button>
                      <span className="w-9 text-center text-sm text-[#222]">{item.quantity}</span>
                      <button
                        onClick={() => updateQuantity(item.cartId, item.quantity + 1)}
                        className="w-8 h-8 flex items-center justify-center text-[#555] hover:bg-[#f5f5f5]"
                      >
                        +
                      </button>
                    </div>
                  </div>

                  <div className="w-28 text-right text-sm font-semibold text-[#222]">
                    {formatPrice(item.price * item.quantity)}
                  </div>

                  <button
                    onClick={() => removeItem(item.cartId)}
                    className="w-10 flex justify-end text-[#bbb] hover:text-[#555] transition-colors"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>

            <div className="lg:w-80 flex-shrink-0">
              <div className="border border-[#e5e5e5] p-6 sticky top-24">
                <h2 className="text-sm font-bold text-[#222] mb-5 pb-3 border-b border-[#e5e5e5]">
                  주문 금액
                </h2>
                <div className="space-y-3 text-sm mb-5">
                  <div className="flex justify-between">
                    <span className="text-[#777]">상품 금액</span>
                    <span>{formatPrice(totalPrice)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-[#777]">배송비</span>
                    <span>{shippingFee === 0 ? '무료' : formatPrice(shippingFee)}</span>
                  </div>
                </div>
                <div className="flex justify-between font-bold text-base border-t border-[#e5e5e5] pt-4 mb-6">
                  <span>결제 예정 금액</span>
                  <span className="text-[#222]">{formatPrice(finalPrice)}</span>
                </div>
                <button disabled={selectedItems.length === 0} onClick={() => { sessionStorage.setItem('checkout_cart_ids', JSON.stringify(selectedIds)); window.location.href = '/orders/checkout?mode=cart'; }} className="w-full bg-[#222] py-3 text-sm font-medium text-white disabled:opacity-40">선택 상품 주문하기 ({selectedItems.length})</button>
                <Link href="/products" className="block text-center text-xs text-[#999] mt-3 hover:text-[#555] transition-colors">
                  쇼핑 계속하기
                </Link>
              </div>
            </div>
          </div>
        )}
      </main>

      <ShopFooter />
    </>
  );
}
