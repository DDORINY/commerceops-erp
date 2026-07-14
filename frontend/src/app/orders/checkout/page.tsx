'use client';

import { useState, useEffect } from 'react';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import { cartService, type ApiCartItem } from '@/lib/services/cartService';
import { orderService } from '@/lib/services/orderService';
import { paymentService } from '@/lib/services/paymentService';
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';
import { couponService, type CouponValidateResult } from '@/lib/services/couponService';
import { formatPrice } from '@/lib/format';

const PAYMENT_METHODS = [{ value: 'TOSS_CARD', label: '토스페이먼츠 카드 결제' }];

export default function CheckoutPage() {
  const [cartItems, setCartItems] = useState<ApiCartItem[]>([]);
  const [cartLoading, setCartLoading] = useState(true);
  const [cartError, setCartError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({
    receiverName: '',
    receiverPhone: '',
    zipCode: '',
    address: '',
    addressDetail: '',
    paymentMethod: 'TOSS_CARD',
  });
  const [couponCode, setCouponCode] = useState('');
  const [couponResult, setCouponResult] = useState<CouponValidateResult | null>(null);
  const [couponError, setCouponError] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [validatingCoupon, setValidatingCoupon] = useState(false);

  useEffect(() => {
    cartService
      .getCart()
      .then((cart) => {
        setCartItems(cart.items);
        setCartError('');
      })
      .catch(() => {
        setCartItems([]);
        setCartError('주문 상품을 불러오지 못했습니다. 장바구니를 확인해주세요.');
      })
      .finally(() => setCartLoading(false));
  }, []);

  const totalPrice = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const discountAmount = couponResult?.discountAmount ?? 0;
  const shippingFee = totalPrice >= 50000 ? 0 : 3000;
  const finalPrice = totalPrice - discountAmount + shippingFee;

  const handleChange = (field: string, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleCouponApply = async () => {
    if (!couponCode.trim()) return;
    setValidatingCoupon(true);
    setCouponError('');
    setCouponResult(null);
    couponService
      .validate(couponCode.trim(), totalPrice)
      .then((res) => setCouponResult(res))
      .catch((err) => setCouponError(err instanceof Error ? err.message : '유효하지 않은 쿠폰입니다.'))
      .finally(() => setValidatingCoupon(false));
  };

  const handleCouponRemove = () => {
    setCouponCode('');
    setCouponResult(null);
    setCouponError('');
  };

  const handleSubmit = async () => {
    setSubmitError('');
    if (!form.receiverName || !form.receiverPhone || !form.address) {
      setSubmitError('배송 정보를 모두 입력해주세요.');
      return;
    }
    if (cartItems.length === 0) {
      setSubmitError('장바구니가 비어 있습니다.');
      return;
    }

    try {
      setSubmitting(true);
      const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY;
      if (!clientKey) throw new Error('토스페이먼츠 문서용 테스트 클라이언트 키가 설정되지 않았습니다.');
      if (!clientKey.startsWith('test_gck_')) {
        throw new Error('라이브 키는 사용할 수 없습니다. 문서용 test_gck 키를 설정해주세요.');
      }
      const orderRes = await orderService.createOrder({
        receiverName: form.receiverName,
        receiverPhone: form.receiverPhone,
        address: form.address,
        detailAddress: form.addressDetail || undefined,
        paymentMethod: form.paymentMethod,
        cartItemIds: cartItems.map((item) => item.cartId),
        couponCode: couponResult ? couponResult.code : undefined,
      });

      const prepared = await paymentService.prepareToss(orderRes.orderId);
      const tossPayments = await loadTossPayments(clientKey);
      const widgets = tossPayments.widgets({ customerKey: prepared.customerKey });
      await widgets.setAmount({ currency: 'KRW', value: prepared.amount });
      const paymentWindow = await widgets.renderPaymentWindow();
      paymentWindow.on('paymentRequest', async () => widgets.requestPayment({
        orderId: prepared.paymentOrderId,
        orderName: prepared.orderName,
        customerName: prepared.customerName,
        customerEmail: prepared.customerEmail,
        successUrl: `${window.location.origin}/payments/success`,
        failUrl: `${window.location.origin}/payments/fail`,
      }));
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : '주문 처리에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ShopHeader />

      <main className="max-w-[1200px] mx-auto px-4 py-10">
        <h1 className="text-xl font-bold text-[#222] mb-8 pb-4 border-b border-[#e5e5e5]">
          주문/결제
        </h1>

        <div className="mb-8 border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          <strong>TEST MODE</strong> · 포트폴리오 시연용 가상 결제입니다. 실제 청구와 정산은 발생하지 않습니다.
        </div>

        <div className="flex flex-col lg:flex-row gap-8">
          <div className="flex-1 space-y-8">
            <section>
              <h2 className="text-sm font-bold text-[#222] mb-4 pb-2 border-b border-[#e5e5e5]">
                주문 상품
              </h2>
              {cartLoading ? (
                <p className="text-sm text-[#aaa]">상품 목록을 불러오는 중...</p>
              ) : cartError ? (
                <p className="text-sm text-[#c43a3a]">{cartError}</p>
              ) : cartItems.length === 0 ? (
                <p className="text-sm text-[#aaa]">주문할 상품이 없습니다.</p>
              ) : (
                <div className="space-y-3">
                  {cartItems.map((item) => (
                    <div key={item.cartId} className="flex items-center gap-3 text-sm">
                      <div className="w-14 h-16 bg-[#f7f7f7] flex-shrink-0" />
                      <div className="flex-1">
                        <p className="text-[#222] font-medium">{item.productName}</p>
                        <p className="text-[#999] text-xs mt-0.5">{item.quantity}개</p>
                      </div>
                      <span className="font-semibold">{formatPrice(item.price * item.quantity)}</span>
                    </div>
                  ))}
                </div>
              )}
            </section>

            <section>
              <h2 className="text-sm font-bold text-[#222] mb-4 pb-2 border-b border-[#e5e5e5]">
                배송지 정보
              </h2>
              <div className="space-y-4">
                <Input
                  label="받는 분"
                  value={form.receiverName}
                  onChange={(e) => handleChange('receiverName', e.target.value)}
                  placeholder="이름 입력"
                  fullWidth
                />
                <Input
                  label="연락처"
                  value={form.receiverPhone}
                  onChange={(e) => handleChange('receiverPhone', e.target.value)}
                  placeholder="010-0000-0000"
                  fullWidth
                />
                <div className="flex gap-3">
                  <Input
                    label="우편번호"
                    value={form.zipCode}
                    onChange={(e) => handleChange('zipCode', e.target.value)}
                    placeholder="우편번호"
                    className="w-36"
                  />
                  <div className="flex items-end">
                    <Button variant="outline" size="sm">
                      주소 검색
                    </Button>
                  </div>
                </div>
                <Input
                  label="주소"
                  value={form.address}
                  onChange={(e) => handleChange('address', e.target.value)}
                  placeholder="기본 주소"
                  fullWidth
                />
                <Input
                  value={form.addressDetail}
                  onChange={(e) => handleChange('addressDetail', e.target.value)}
                  placeholder="상세 주소"
                  fullWidth
                />
              </div>
            </section>

            <section>
              <h2 className="text-sm font-bold text-[#222] mb-4 pb-2 border-b border-[#e5e5e5]">
                쿠폰
              </h2>
              {couponResult ? (
                <div className="flex items-center justify-between bg-[#f0fdf4] border border-[#bbf7d0] px-4 py-3 text-sm">
                  <div>
                    <span className="font-bold text-[#16a34a]">{couponResult.code}</span>
                    <span className="text-[#555] ml-2">적용됨 — {formatPrice(couponResult.discountAmount)} 할인</span>
                  </div>
                  <button onClick={handleCouponRemove} className="text-xs text-[#999] hover:text-[#444] underline">
                    제거
                  </button>
                </div>
              ) : (
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={couponCode}
                    onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                    onKeyDown={(e) => e.key === 'Enter' && handleCouponApply()}
                    placeholder="쿠폰 코드 입력"
                    className="flex-1 border border-[#ddd] px-3 py-2 text-sm outline-none focus:border-[#222] uppercase"
                  />
                  <Button variant="outline" size="sm" onClick={handleCouponApply} disabled={validatingCoupon || !couponCode.trim()}>
                    {validatingCoupon ? '확인 중...' : '적용'}
                  </Button>
                </div>
              )}
              {couponError && <p className="text-xs text-[#d94f4f] mt-2">{couponError}</p>}
            </section>

            <section>
              <h2 className="text-sm font-bold text-[#222] mb-4 pb-2 border-b border-[#e5e5e5]">
                결제 수단
              </h2>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                {PAYMENT_METHODS.map((method) => (
                  <button
                    key={method.value}
                    onClick={() => handleChange('paymentMethod', method.value)}
                    className={[
                      'py-3 text-sm border transition-colors',
                      form.paymentMethod === method.value
                        ? 'border-[#222] bg-[#222] text-white'
                        : 'border-[#ddd] text-[#555] hover:border-[#222]',
                    ].join(' ')}
                  >
                    {method.label}
                  </button>
                ))}
              </div>
            </section>
          </div>

          <div className="lg:w-80 flex-shrink-0">
            <div className="border border-[#e5e5e5] p-6 sticky top-24">
              <h2 className="text-sm font-bold text-[#222] mb-5 pb-3 border-b border-[#e5e5e5]">
                최종 결제 금액
              </h2>
              <div className="space-y-3 text-sm mb-5">
                <div className="flex justify-between">
                  <span className="text-[#777]">상품 금액</span>
                  <span>{formatPrice(totalPrice)}</span>
                </div>
                {discountAmount > 0 && (
                  <div className="flex justify-between text-[#16a34a]">
                    <span>쿠폰 할인</span>
                    <span>-{formatPrice(discountAmount)}</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-[#777]">배송비</span>
                  <span>{shippingFee === 0 ? '무료' : formatPrice(shippingFee)}</span>
                </div>
              </div>
              <div className="flex justify-between font-bold text-base border-t border-[#e5e5e5] pt-4 mb-6">
                <span>결제 금액</span>
                <span>{formatPrice(finalPrice)}</span>
              </div>
              <Button
                variant="secondary"
                size="lg"
                fullWidth
                onClick={handleSubmit}
                disabled={submitting || cartLoading || !!cartError || cartItems.length === 0}
              >
                {submitting ? '처리 중...' : '테스트 결제하기'}
              </Button>
              {submitError && (
                <p className="text-xs text-[#c43a3a] mt-3 text-center leading-relaxed">
                  {submitError}
                </p>
              )}
              <p className="text-[10px] text-[#bbb] mt-3 text-center leading-relaxed">
                위 내용을 확인하였으며 결제에 동의합니다.
              </p>
            </div>
          </div>
        </div>
      </main>

      <ShopFooter />
    </>
  );
}
