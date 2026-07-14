'use client';

import { Suspense, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';
import { paymentService } from '@/lib/services/paymentService';

function RetryContent() {
  const params = useSearchParams();
  const orderId = Number(params.get('orderId'));
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const retry = async () => {
    const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY;
    if (!Number.isInteger(orderId) || orderId <= 0) return setError('주문 정보가 올바르지 않습니다.');
    if (!clientKey) return setError('토스페이먼츠 문서용 테스트 클라이언트 키가 설정되지 않았습니다.');
    if (!clientKey.startsWith('test_gck_')) return setError('문서용 test_gck 키만 사용할 수 있습니다.');
    try {
      setLoading(true); setError('');
      const prepared = await paymentService.prepareToss(orderId);
      const toss = await loadTossPayments(clientKey);
      const widgets = toss.widgets({ customerKey: prepared.customerKey });
      await widgets.setAmount({ currency: 'KRW', value: prepared.amount });
      const paymentWindow = await widgets.renderPaymentWindow();
      paymentWindow.on('paymentRequest', async () => widgets.requestPayment({
        orderId: prepared.paymentOrderId, orderName: prepared.orderName,
        customerName: prepared.customerName, customerEmail: prepared.customerEmail,
        successUrl: `${window.location.origin}/payments/success`, failUrl: `${window.location.origin}/payments/fail`,
      }));
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : '결제 재시도에 실패했습니다.');
      setLoading(false);
    }
  };

  return <main className="max-w-xl mx-auto px-4 py-24 text-center">
    <h1 className="text-xl font-bold mb-3">결제 다시 시도</h1>
    <p className="border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900 mb-5"><strong>TEST MODE</strong> · 실제 청구가 없는 가상 승인입니다.</p>
    <p className="text-gray-500 mb-8">서버에 저장된 주문 금액으로 새 결제 요청을 준비합니다.</p>
    <button onClick={retry} disabled={loading} className="bg-black text-white px-6 py-3 disabled:opacity-50">{loading ? '결제창 준비 중...' : '테스트 결제위젯 열기'}</button>
    {error && <p className="text-red-600 mt-4">{error}</p>}
    <div className="mt-6"><Link href={Number.isInteger(orderId) ? `/orders/${orderId}` : '/orders'} className="underline">주문 상세로 돌아가기</Link></div>
  </main>;
}

export default function PaymentRetryPage() {
  return <Suspense fallback={<main className="p-20 text-center">주문을 확인하고 있습니다.</main>}><RetryContent /></Suspense>;
}
