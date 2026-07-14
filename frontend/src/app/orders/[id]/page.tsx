'use client';

import { use, useState, useEffect } from 'react';
import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import Button from '@/components/common/Button';
import { orderService, type ApiOrderDetail } from '@/lib/services/orderService';
import { shipmentService, type ApiShipment } from '@/lib/services/shipmentService';
import { returnService, type ApiReturn } from '@/lib/services/returnService';
import { reviewService, type ApiReview } from '@/lib/services/reviewService';
import {
  formatPrice,
  formatDateTime,
  ORDER_STATUS_LABEL,
  ORDER_STATUS_COLOR,
  SHIPMENT_STATUS_LABEL,
  RETURN_STATUS_LABEL,
  RETURN_STATUS_COLOR,
  RETURN_REASON_LABEL,
} from '@/lib/format';

const RETURN_REASONS = [
  { value: 'CHANGE_OF_MIND', label: '단순 변심' },
  { value: 'DEFECTIVE', label: '불량/파손' },
  { value: 'WRONG_DELIVERY', label: '오배송' },
];

export default function OrderDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const [order, setOrder] = useState<ApiOrderDetail | null>(null);
  const [shipment, setShipment] = useState<ApiShipment | null>(null);
  const [returnData, setReturnData] = useState<ApiReturn | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [showReturnForm, setShowReturnForm] = useState(false);
  const [returnReason, setReturnReason] = useState('CHANGE_OF_MIND');
  const [returnDetail, setReturnDetail] = useState('');
  const [submittingReturn, setSubmittingReturn] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [myReviews, setMyReviews] = useState<ApiReview[]>([]);
  const [reviewForms, setReviewForms] = useState<Record<number, { rating: number; content: string; submitting: boolean }>>({});

  useEffect(() => {
    orderService
      .getOrderDetail(Number(id))
      .then((data) => {
        setOrder(data);
        // 배송 정보 조회 (PREPARING 이상 상태에서만 존재)
        if (['PREPARING', 'SHIPPING', 'COMPLETED'].includes(data.status)) {
          shipmentService.getOrderShipment(Number(id)).then(setShipment).catch(() => {});
        }
        returnService.getMyReturns()
          .then((list) => {
            const found = list.find((r) => r.orderId === Number(id));
            if (found) setReturnData(found);
          })
          .catch(() => {});
        reviewService.getMyReviews().then(setMyReviews).catch(() => {});
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <>
        <ShopHeader />
        <div className="max-w-[1200px] mx-auto px-4 py-20 text-center text-[#aaa] text-sm">
          주문 정보를 불러오는 중...
        </div>
        <ShopFooter />
      </>
    );
  }

  if (notFound || !order) {
    return (
      <>
        <ShopHeader />
        <div className="max-w-[1200px] mx-auto px-4 py-20 text-center text-[#aaa]">
          주문을 찾을 수 없습니다.
          <br />
          <Link href="/orders" className="text-[#222] underline mt-4 inline-block">
            주문 목록으로
          </Link>
        </div>
        <ShopFooter />
      </>
    );
  }

  return (
    <>
      <ShopHeader />

      <main className="max-w-[800px] mx-auto px-4 py-10">
        <div className="flex items-center gap-2 text-xs text-[#999] mb-6">
          <Link href="/orders" className="hover:text-[#222]">주문 내역</Link>
          <span>/</span>
          <span className="text-[#444]">주문 상세</span>
        </div>

        <div className="flex items-center justify-between mb-8 pb-4 border-b border-[#e5e5e5]">
          <h1 className="text-xl font-bold text-[#222]">주문 상세</h1>
          <span className={`text-xs font-medium px-2.5 py-1 ${ORDER_STATUS_COLOR[order.status] ?? ''}`}>
            {ORDER_STATUS_LABEL[order.status] ?? order.status}
          </span>
        </div>

        {['PENDING_PAYMENT', 'PAYMENT_FAILED', 'PENDING', 'PAID', 'PREPARING'].includes(order.status) && (
          <div className="flex justify-end gap-2 mb-5">
            {['PENDING_PAYMENT', 'PAYMENT_FAILED', 'PENDING'].includes(order.status) && (
              <Link
                href={`/payments/retry?orderId=${order.orderId}`}
                className="inline-flex items-center justify-center bg-[#222] px-4 py-2 text-xs font-medium text-white hover:bg-black"
              >
                {order.status === 'PAYMENT_FAILED' ? '결제 다시 시도' : '결제 계속하기'}
              </Link>
            )}
            <Button
              variant="danger"
              size="sm"
              disabled={cancelling}
              onClick={async () => {
                const message = ['PENDING_PAYMENT', 'PAYMENT_FAILED', 'PENDING'].includes(order.status)
                  ? '이 주문을 취소하시겠습니까?'
                  : '결제 환불과 재고 복구를 포함해 주문을 취소하시겠습니까?';
                if (!confirm(message)) return;
                try {
                  setCancelling(true);
                  await orderService.cancelOrder(order.orderId);
                  const awaitingPayment = ['PENDING_PAYMENT', 'PAYMENT_FAILED', 'PENDING'].includes(order.status);
                  setOrder({ ...order, status: 'CANCELLED', paymentStatus: awaitingPayment ? 'CANCELLED' : 'REFUNDED' });
                } catch (error) {
                  alert(error instanceof Error ? error.message : '주문 취소에 실패했습니다.');
                } finally {
                  setCancelling(false);
                }
              }}
            >
              {cancelling ? '취소 처리 중...' : '주문 취소'}
            </Button>
          </div>
        )}

        <div className="space-y-6">
          <section className="border border-[#e5e5e5] p-5">
            <h2 className="text-sm font-bold text-[#222] mb-4 pb-2 border-b border-[#f0f0f0]">주문 정보</h2>
            <div className="space-y-2 text-sm">
              <div className="flex gap-4">
                <span className="w-24 text-[#999]">주문번호</span>
                <span>{order.orderNumber}</span>
              </div>
              <div className="flex gap-4">
                <span className="w-24 text-[#999]">주문일시</span>
                <span>{formatDateTime(order.createdAt)}</span>
              </div>
              <div className="flex gap-4">
                <span className="w-24 text-[#999]">결제상태</span>
                <span>{order.paymentStatus}</span>
              </div>
              {order.payment && <>
                <div className="flex gap-4"><span className="w-24 text-[#999]">결제 제공사</span><span>{order.payment.provider || '-'}</span></div>
                <div className="flex gap-4"><span className="w-24 text-[#999]">결제 수단</span><span>{order.payment.method || '-'}</span></div>
                <div className="flex gap-4"><span className="w-24 text-[#999]">승인 금액</span><span>{formatPrice(order.payment.amount || 0)}</span></div>
                {order.payment.approvedAt && <div className="flex gap-4"><span className="w-24 text-[#999]">승인 일시</span><span>{formatDateTime(order.payment.approvedAt)}</span></div>}
                {order.payment.failureMessage && <div className="flex gap-4 text-red-600"><span className="w-24">실패 사유</span><span>{order.payment.failureMessage}</span></div>}
              </>}
            </div>
          </section>

          {shipment && (
            <section className="border border-[#e5e5e5] p-5">
              <h2 className="text-sm font-bold text-[#222] mb-4 pb-2 border-b border-[#f0f0f0]">배송 현황</h2>
              <div className="space-y-2 text-sm">
                <div className="flex gap-4">
                  <span className="w-24 text-[#999]">배송 상태</span>
                  <span className="font-medium">{SHIPMENT_STATUS_LABEL[shipment.status] ?? shipment.status}</span>
                </div>
                {shipment.carrier && (
                  <div className="flex gap-4">
                    <span className="w-24 text-[#999]">택배사</span>
                    <span>{shipment.carrier}</span>
                  </div>
                )}
                {shipment.trackingNumber && (
                  <div className="flex gap-4">
                    <span className="w-24 text-[#999]">송장번호</span>
                    <span className="font-mono">{shipment.trackingNumber}</span>
                  </div>
                )}
                {shipment.shippedAt && (
                  <div className="flex gap-4">
                    <span className="w-24 text-[#999]">배송 시작</span>
                    <span>{formatDateTime(shipment.shippedAt)}</span>
                  </div>
                )}
                {shipment.deliveredAt && (
                  <div className="flex gap-4">
                    <span className="w-24 text-[#999]">배송 완료</span>
                    <span>{formatDateTime(shipment.deliveredAt)}</span>
                  </div>
                )}
              </div>
            </section>
          )}

          <section className="border border-[#e5e5e5] p-5">
            <h2 className="text-sm font-bold text-[#222] mb-4 pb-2 border-b border-[#f0f0f0]">배송지 정보</h2>
            <div className="space-y-2 text-sm">
              <div className="flex gap-4">
                <span className="w-24 text-[#999]">받는 분</span>
                <span>{order.receiverName}</span>
              </div>
              <div className="flex gap-4">
                <span className="w-24 text-[#999]">연락처</span>
                <span>{order.receiverPhone}</span>
              </div>
              <div className="flex gap-4">
                <span className="w-24 text-[#999]">주소</span>
                <span>{order.address} {order.detailAddress}</span>
              </div>
            </div>
          </section>

          <section className="border border-[#e5e5e5] p-5">
            <h2 className="text-sm font-bold text-[#222] mb-4 pb-2 border-b border-[#f0f0f0]">주문 상품</h2>
            <div className="space-y-4">
              {order.items.map((item) => {
                const existingReview = myReviews.find((r) => r.orderItemId === item.orderItemId);
                const form = reviewForms[item.orderItemId];
                const canReview = order.status === 'COMPLETED';
                return (
                  <div key={item.orderItemId} className="border-b border-[#f5f5f5] pb-4 last:border-0 last:pb-0">
                    <div className="flex items-center justify-between text-sm mb-2">
                      <div>
                        <p className="text-[#222] font-medium">{item.productName}</p>
                        <p className="text-[#999] text-xs mt-0.5">{item.quantity}개 × {formatPrice(item.price)}</p>
                      </div>
                      <span className="font-semibold">{formatPrice(item.subtotal)}</span>
                    </div>
                    {canReview && (
                      existingReview ? (
                        <div className="mt-1 bg-[#f9f9f9] border border-[#eee] px-3 py-2 text-xs text-[#555]">
                          <span className="text-yellow-500">{'★'.repeat(existingReview.rating)}{'☆'.repeat(5 - existingReview.rating)}</span>
                          <span className="ml-2 text-[#777]">{existingReview.content || '(내용 없음)'}</span>
                          <span className="ml-2 text-[#bbb]">리뷰 완료</span>
                        </div>
                      ) : form ? (
                        <div className="mt-2 space-y-2">
                          <div className="flex gap-1">
                            {[1,2,3,4,5].map((s) => (
                              <button
                                key={s}
                                type="button"
                                onClick={() => setReviewForms((prev) => ({ ...prev, [item.orderItemId]: { ...prev[item.orderItemId], rating: s } }))}
                                className={`text-xl ${s <= (form.rating || 0) ? 'text-yellow-400' : 'text-[#ddd]'}`}
                              >★</button>
                            ))}
                            <span className="text-xs text-[#999] ml-2 self-center">{form.rating > 0 ? `${form.rating}점` : '평점 선택'}</span>
                          </div>
                          <textarea
                            value={form.content}
                            onChange={(e) => setReviewForms((prev) => ({ ...prev, [item.orderItemId]: { ...prev[item.orderItemId], content: e.target.value } }))}
                            placeholder="리뷰 내용을 입력하세요 (선택)"
                            rows={2}
                            className="w-full border border-[#e0e0e0] px-3 py-1.5 text-xs outline-none focus:border-[#222] resize-none"
                          />
                          <div className="flex gap-2">
                            <Button
                              variant="primary"
                              size="sm"
                              disabled={!form.rating || form.submitting}
                              onClick={async () => {
                                setReviewForms((prev) => ({ ...prev, [item.orderItemId]: { ...prev[item.orderItemId], submitting: true } }));
                                try {
                                  const result = await reviewService.createReview(Number(id), item.orderItemId, form.rating, form.content);
                                  setMyReviews((prev) => [...prev, result]);
                                  setReviewForms((prev) => { const n = { ...prev }; delete n[item.orderItemId]; return n; });
                                } catch (err) {
                                  alert(err instanceof Error ? err.message : '리뷰 등록에 실패했습니다.');
                                  setReviewForms((prev) => ({ ...prev, [item.orderItemId]: { ...prev[item.orderItemId], submitting: false } }));
                                }
                              }}
                            >
                              {form.submitting ? '등록 중...' : '리뷰 등록'}
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => setReviewForms((prev) => { const n = { ...prev }; delete n[item.orderItemId]; return n; })}
                            >취소</Button>
                          </div>
                        </div>
                      ) : (
                        <button
                          type="button"
                          onClick={() => setReviewForms((prev) => ({ ...prev, [item.orderItemId]: { rating: 0, content: '', submitting: false } }))}
                          className="mt-1 text-xs text-[#4c74e5] underline hover:text-[#2a55d0]"
                        >
                          리뷰 작성
                        </button>
                      )
                    )}
                  </div>
                );
              })}
            </div>
            <div className="flex justify-between font-bold text-base border-t border-[#f0f0f0] pt-4 mt-4">
              <span>총 결제금액</span>
              <span>{formatPrice(order.totalPrice)}</span>
            </div>
          </section>
          {/* 반품 현황 또는 신청 */}
          {(order.status === 'SHIPPING' || order.status === 'COMPLETED') && (
            <section className="border border-[#e5e5e5] p-5">
              <h2 className="text-sm font-bold text-[#222] mb-4 pb-2 border-b border-[#f0f0f0]">반품 신청</h2>

              {returnData ? (
                <div className="space-y-2 text-sm">
                  <div className="flex gap-4">
                    <span className="w-24 text-[#999]">반품 상태</span>
                    <span className={`text-xs font-medium px-2 py-0.5 ${RETURN_STATUS_COLOR[returnData.status] ?? ''}`}>
                      {RETURN_STATUS_LABEL[returnData.status] ?? returnData.status}
                    </span>
                  </div>
                  <div className="flex gap-4">
                    <span className="w-24 text-[#999]">반품 사유</span>
                    <span>{RETURN_REASON_LABEL[returnData.reason] ?? returnData.reason}</span>
                  </div>
                  {returnData.adminNote && (
                    <div className="flex gap-4">
                      <span className="w-24 text-[#999]">관리자 메모</span>
                      <span>{returnData.adminNote}</span>
                    </div>
                  )}
                </div>
              ) : showReturnForm ? (
                <div className="space-y-3">
                  <div>
                    <label className="block text-xs text-[#999] mb-1">반품 사유</label>
                    <select
                      value={returnReason}
                      onChange={(e) => setReturnReason(e.target.value)}
                      className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#222]"
                    >
                      {RETURN_REASONS.map((r) => (
                        <option key={r.value} value={r.value}>{r.label}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs text-[#999] mb-1">상세 사유 (선택)</label>
                    <input
                      type="text"
                      value={returnDetail}
                      onChange={(e) => setReturnDetail(e.target.value)}
                      placeholder="예: 사이즈가 맞지 않습니다"
                      className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#222]"
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="primary"
                      size="sm"
                      disabled={submittingReturn}
                      onClick={async () => {
                        setSubmittingReturn(true);
                        try {
                          const result = await returnService.createReturn(Number(id), returnReason, returnDetail || undefined);
                          setReturnData(result);
                          setShowReturnForm(false);
                        } catch (err) {
                          alert(err instanceof Error ? err.message : '반품 신청에 실패했습니다.');
                        } finally {
                          setSubmittingReturn(false);
                        }
                      }}
                    >
                      {submittingReturn ? '신청 중...' : '반품 신청'}
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => setShowReturnForm(false)}>
                      취소
                    </Button>
                  </div>
                </div>
              ) : (
                <div>
                  <p className="text-sm text-[#777] mb-3">수령 후 7일 이내 반품 신청이 가능합니다.</p>
                  <Button variant="outline" size="sm" onClick={() => setShowReturnForm(true)}>
                    반품 신청하기
                  </Button>
                </div>
              )}
            </section>
          )}
        </div>
      </main>

      <ShopFooter />
    </>
  );
}
