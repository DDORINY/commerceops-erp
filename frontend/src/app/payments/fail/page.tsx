'use client';

import { Suspense } from 'react';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';

function FailContent() {
  const params = useSearchParams();
  const message = params.get('message') || '결제가 취소되었거나 인증에 실패했습니다.';
  const orderId = params.get('orderId');
  const numericOrderId = orderId?.match(/^ORD-(\d+)-/)?.[1];
  return <main className="max-w-xl mx-auto px-4 py-24 text-center">
    <p className="border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900 mb-6"><strong>TEST MODE</strong> · 실제 청구와 정산은 발생하지 않습니다.</p>
    <h1 className="text-xl font-bold mb-3">결제에 실패했습니다</h1>
    <p className="text-red-600 mb-2">{message}</p>
    {params.get('code') && <p className="text-xs text-gray-500 mb-8">오류 코드: {params.get('code')}</p>}
    <div className="flex justify-center gap-3">
      <Link href={numericOrderId ? `/payments/retry?orderId=${numericOrderId}` : '/orders'} className="border px-4 py-2">다시 결제하기</Link>
      <Link href={numericOrderId ? `/orders/${numericOrderId}` : '/orders'} className="bg-black text-white px-4 py-2">주문 상세 이동</Link>
    </div>
  </main>;
}

export default function PaymentFailPage() {
  return <Suspense fallback={<main className="p-20 text-center">결제 결과를 확인하고 있습니다.</main>}><FailContent /></Suspense>;
}
