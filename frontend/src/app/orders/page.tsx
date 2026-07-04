'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import { orderService, type ApiOrder } from '@/lib/services/orderService';
import { formatPrice, formatDate, ORDER_STATUS_LABEL, ORDER_STATUS_COLOR } from '@/lib/format';

export default function OrdersPage() {
  const [orders, setOrders] = useState<ApiOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    orderService
      .getOrders()
      .then((res) => {
        setOrders(res);
        setErrorMessage('');
      })
      .catch(() => {
        setOrders([]);
        setErrorMessage('주문 내역을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <ShopHeader />

      <main className="max-w-[1200px] mx-auto px-4 py-10">
        <h1 className="text-xl font-bold text-[#222] mb-8 pb-4 border-b border-[#e5e5e5]">
          주문 내역
        </h1>

        {loading ? (
          <div className="py-24 text-center text-[#aaa] text-sm">
            주문 내역을 불러오는 중...
          </div>
        ) : errorMessage ? (
          <div className="py-24 text-center text-[#c43a3a] text-sm">
            {errorMessage}
          </div>
        ) : orders.length === 0 ? (
          <div className="py-24 text-center text-[#aaa] text-sm">
            주문 내역이 없습니다.
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div key={order.orderId} className="border border-[#e5e5e5] p-6">
                <div className="flex items-center justify-between mb-4 pb-3 border-b border-[#f0f0f0]">
                  <div className="flex items-center gap-4">
                    <span className="text-sm font-medium text-[#222]">{order.orderNumber}</span>
                    <span className="text-xs text-[#999]">{formatDate(order.createdAt)}</span>
                  </div>
                  <span className={`text-xs font-medium px-2.5 py-1 ${ORDER_STATUS_COLOR[order.status] ?? ''}`}>
                    {ORDER_STATUS_LABEL[order.status] ?? order.status}
                  </span>
                </div>

                <div className="flex items-center justify-between pt-3">
                  <span className="text-sm text-[#999]">총 결제금액</span>
                  <span className="text-base font-bold text-[#222]">
                    {formatPrice(order.totalPrice)}
                  </span>
                </div>

                <div className="mt-3 flex justify-end">
                  <Link
                    href={`/orders/${order.orderId}`}
                    className="text-xs text-[#777] hover:text-[#222] underline transition-colors"
                  >
                    주문 상세
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      <ShopFooter />
    </>
  );
}
