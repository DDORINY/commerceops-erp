'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import { orderService, type ApiOrder } from '@/lib/services/orderService';
import { inquiryService, type ApiInquiry } from '@/lib/services/inquiryService';
import { returnService, type ApiReturn } from '@/lib/services/returnService';
import {
  formatPrice,
  formatDate,
  formatDateTime,
  ORDER_STATUS_LABEL,
  ORDER_STATUS_COLOR,
  INQUIRY_STATUS_LABEL,
  INQUIRY_STATUS_COLOR,
  RETURN_STATUS_LABEL,
  RETURN_STATUS_COLOR,
  RETURN_REASON_LABEL,
} from '@/lib/format';
import { authService, type MeResponse } from '@/lib/services/authService';

export default function MyPage() {
  const [user, setUser] = useState<MeResponse | null>(null);
  const [orders, setOrders] = useState<ApiOrder[]>([]);
  const [inquiries, setInquiries] = useState<ApiInquiry[]>([]);
  const [returns, setReturns] = useState<ApiReturn[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activityError, setActivityError] = useState('');
  const [activeTab, setActiveTab] = useState<'orders' | 'inquiries' | 'returns'>('orders');

  useEffect(() => {
    let mounted = true;

    const loadMyPage = async () => {
      setLoading(true);
      setError('');
      setActivityError('');

      try {
        const [me, orderList] = await Promise.all([
          authService.me(),
          orderService.getOrders(),
        ]);

        if (!mounted) return;
        setUser(me);
        setOrders(orderList);

        const [inquiryResult, returnResult] = await Promise.allSettled([
          inquiryService.getMyInquiries(),
          returnService.getMyReturns(),
        ]);

        if (!mounted) return;
        if (inquiryResult.status === 'fulfilled') {
          setInquiries(inquiryResult.value);
        }
        if (returnResult.status === 'fulfilled') {
          setReturns(returnResult.value);
        }
        if (inquiryResult.status === 'rejected' || returnResult.status === 'rejected') {
          setActivityError('일부 활동 내역을 불러오지 못했습니다.');
        }
      } catch (err) {
        if (!mounted) return;
        setError(err instanceof Error ? err.message : '마이페이지 정보를 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadMyPage();

    return () => {
      mounted = false;
    };
  }, []);

  const shippingCount = orders.filter((o) => o.status === 'SHIPPING').length;
  const completedCount = orders.filter((o) => o.status === 'COMPLETED').length;
  const userName = user?.name ?? '';
  const userEmail = user?.email ?? '';

  return (
    <>
      <ShopHeader />

      <main className="max-w-[1200px] mx-auto px-4 py-10">
        <h1 className="text-xl font-bold text-[#222] mb-8 pb-4 border-b border-[#e5e5e5]">
          마이페이지
        </h1>

        {loading ? (
          <div className="py-24 text-center text-[#aaa] text-sm">
            마이페이지 정보를 불러오는 중...
          </div>
        ) : error ? (
          <div className="border border-[#f1c7c7] bg-[#fff7f7] px-5 py-6 text-center">
            <p className="text-sm text-[#c43a3a]">{error}</p>
            <Link href="/login" className="mt-3 inline-block text-xs text-[#777] underline hover:text-[#222]">
              로그인 페이지로 이동
            </Link>
          </div>
        ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 회원 정보 */}
          <div className="lg:col-span-1">
            <div className="border border-[#e5e5e5] p-6">
              <div className="flex items-center gap-4 mb-6">
                <div className="w-14 h-14 bg-[#f3a6b8]/20 rounded-full flex items-center justify-center">
                  <span className="text-[#d97b93] text-xl font-bold">
                    {userName ? userName.charAt(0) : '?'}
                  </span>
                </div>
                <div>
                  <p className="font-bold text-[#222]">{userName || '—'}</p>
                  <p className="text-sm text-[#999] mt-0.5">{userEmail || '—'}</p>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3 border-t border-[#f0f0f0] pt-5">
                {[
                  { label: '총 주문', value: orders.length },
                  { label: '배송중', value: shippingCount },
                  { label: '완료', value: completedCount },
                ].map((stat) => (
                  <div key={stat.label} className="text-center">
                    <p className="text-xl font-bold text-[#222]">{stat.value}</p>
                    <p className="text-xs text-[#999] mt-0.5">{stat.label}</p>
                  </div>
                ))}
              </div>
              <Link href="/mypage/addresses" className="mt-5 block border border-[#ddd] px-4 py-2.5 text-center text-sm hover:bg-[#fafafa]">배송지 관리</Link>
            </div>
          </div>

          {/* 탭 콘텐츠 */}
          <div className="lg:col-span-2">
            <div className="flex gap-0 mb-5 border-b border-[#e5e5e5]">
              {([
                ['orders', '주문 내역'],
                ['inquiries', `문의 내역 ${inquiries.length > 0 ? `(${inquiries.length})` : ''}`],
                ['returns', `반품 내역 ${returns.length > 0 ? `(${returns.length})` : ''}`],
              ] as const).map(([tab, label]) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={[
                    'px-5 py-2.5 text-sm font-medium transition-colors',
                    activeTab === tab
                      ? 'border-b-2 border-[#222] text-[#222]'
                      : 'text-[#999] hover:text-[#555]',
                  ].join(' ')}
                >
                  {label}
                </button>
              ))}
            </div>

            {activityError && (
              <p className="mb-4 border border-[#f1e0b8] bg-[#fffaf0] px-4 py-2 text-xs text-[#9a6b1f]">
                {activityError}
              </p>
            )}

            {activeTab === 'orders' && (
              <div className="space-y-3">
                {orders.length === 0 ? (
                  <p className="text-sm text-[#bbb] text-center py-8">주문 내역이 없습니다.</p>
                ) : (
                  orders.slice(0, 5).map((order) => (
                    <Link key={order.orderId} href={`/orders/${order.orderId}`}>
                      <div className="border border-[#e5e5e5] p-4 hover:border-[#ccc] transition-colors">
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-sm font-medium text-[#222]">{order.orderNumber}</span>
                          <span className={`text-xs font-medium px-2 py-0.5 ${ORDER_STATUS_COLOR[order.status] ?? ''}`}>
                            {ORDER_STATUS_LABEL[order.status] ?? order.status}
                          </span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="text-xs text-[#999]">{formatDate(order.createdAt)}</span>
                          <span className="text-sm font-bold text-[#222]">{formatPrice(order.totalPrice)}</span>
                        </div>
                      </div>
                    </Link>
                  ))
                )}
                {orders.length > 0 && (
                  <div className="text-center mt-2">
                    <Link href="/orders" className="text-xs text-[#999] underline hover:text-[#222]">
                      전체 주문 보기
                    </Link>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'inquiries' && (
              <div className="space-y-3">
                {inquiries.length === 0 ? (
                  <p className="text-sm text-[#bbb] text-center py-8">문의 내역이 없습니다.</p>
                ) : (
                  inquiries.map((inq) => (
                    <div key={inq.inquiryId} className="border border-[#e5e5e5] p-4">
                      <div className="flex items-center gap-2 mb-1.5">
                        <span className={`text-xs font-medium px-2 py-0.5 ${INQUIRY_STATUS_COLOR[inq.status] ?? ''}`}>
                          {INQUIRY_STATUS_LABEL[inq.status] ?? inq.status}
                        </span>
                        {inq.productName && (
                          <span className="text-xs text-[#777]">[{inq.productName}]</span>
                        )}
                      </div>
                      <p className="text-sm font-medium text-[#222]">{inq.subject}</p>
                      <p className="text-xs text-[#999] mt-1 truncate">{inq.content}</p>
                      {inq.answer && (
                        <div className="mt-2 bg-[#f7f8fc] border-l-2 border-[#4c74e5] px-3 py-1.5 text-xs text-[#555]">
                          <span className="font-bold text-[#4c74e5]">답변: </span>{inq.answer}
                        </div>
                      )}
                      <p className="text-xs text-[#bbb] mt-2">{formatDateTime(inq.createdAt)}</p>
                    </div>
                  ))
                )}
              </div>
            )}

            {activeTab === 'returns' && (
              <div className="space-y-3">
                {returns.length === 0 ? (
                  <p className="text-sm text-[#bbb] text-center py-8">반품 내역이 없습니다.</p>
                ) : (
                  returns.map((r) => (
                    <div key={r.returnId} className="border border-[#e5e5e5] p-4">
                      <div className="flex items-center gap-2 mb-1.5">
                        <span className={`text-xs font-medium px-2 py-0.5 ${RETURN_STATUS_COLOR[r.status] ?? ''}`}>
                          {RETURN_STATUS_LABEL[r.status] ?? r.status}
                        </span>
                        <span className="text-xs text-[#777]">{r.orderNumber}</span>
                      </div>
                      <p className="text-sm text-[#333]">
                        {RETURN_REASON_LABEL[r.reason] ?? r.reason}
                        {r.reasonDetail && <span className="text-[#999]"> — {r.reasonDetail}</span>}
                      </p>
                      {r.adminNote && (
                        <p className="text-xs text-[#777] mt-1">관리자 메모: {r.adminNote}</p>
                      )}
                      <p className="text-xs text-[#bbb] mt-2">{formatDateTime(r.createdAt)}</p>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        </div>
        )}
      </main>

      <ShopFooter />
    </>
  );
}
