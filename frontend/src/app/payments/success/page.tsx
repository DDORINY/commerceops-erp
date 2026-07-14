'use client';

import { Suspense, useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { paymentService } from '@/lib/services/paymentService';
import { notifyCartChanged } from '@/contexts/CartContext';

const confirmations = new Map<string, Promise<number>>();

function SuccessContent() {
  const params = useSearchParams();
  const router = useRouter();
  const [error, setError] = useState('');
  const paymentKey = params.get('paymentKey');
  const orderId = params.get('orderId');
  const amount = Number(params.get('amount'));
  const validationError = !paymentKey || !orderId || !Number.isInteger(amount) || amount <= 0
    ? '결제 승인 정보가 올바르지 않습니다.' : '';

  useEffect(() => {
    if (!paymentKey || !orderId || validationError) return;
    const key = `${paymentKey}:${orderId}:${amount}`;
    let request = confirmations.get(key);
    if (!request) {
      request = paymentService.confirmToss(paymentKey, orderId, amount).then((result) => result.orderId);
      confirmations.set(key, request);
    }
    request.then((numericOrderId) => { notifyCartChanged(); sessionStorage.removeItem('buy_now_item'); sessionStorage.removeItem('checkout_cart_ids'); router.replace(`/orders/${numericOrderId}?payment=success`); })
      .catch((reason) => {
        confirmations.delete(key);
        setError(reason instanceof Error ? reason.message : '결제 승인에 실패했습니다.');
      });
  }, [amount, orderId, paymentKey, router, validationError]);

  return <main className="max-w-xl mx-auto px-4 py-24 text-center">
    <p className="border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900 mb-6"><strong>TEST MODE</strong> · 실제 결제가 아닌 가상 승인을 처리하고 있습니다.</p>
    <h1 className="text-xl font-bold mb-4">{error || validationError ? '결제 승인 실패' : '결제를 승인하고 있습니다'}</h1>
    <p className={error || validationError ? 'text-red-600 mb-6' : 'text-gray-500'}>{error || validationError || '창을 닫거나 새로고침하지 마세요.'}</p>
    {(error || validationError) && <Link href="/orders" className="underline">주문 내역으로 이동</Link>}
  </main>;
}

export default function PaymentSuccessPage() {
  return <Suspense fallback={<main className="p-20 text-center">결제 정보를 확인하고 있습니다.</main>}><SuccessContent /></Suspense>;
}
