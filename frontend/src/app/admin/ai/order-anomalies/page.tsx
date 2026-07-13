'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import DataTable from '@/components/admin/DataTable';
import { formatDateTime, formatPrice } from '@/lib/format';
import { aiOperationsService, type AiInsight, type AiRiskLevel } from '@/lib/services/aiOperationsService';

const RISK_LABEL: Record<AiRiskLevel, string> = {
  LOW: '정상 범위',
  MEDIUM: '확인 필요',
  HIGH: '우선 확인',
};

const RISK_CLASS: Record<AiRiskLevel, string> = {
  LOW: 'bg-green-50 text-green-700 border-green-200',
  MEDIUM: 'bg-yellow-50 text-yellow-700 border-yellow-200',
  HIGH: 'bg-red-50 text-red-700 border-red-200',
};

function featureText(value: unknown) {
  if (value === null || value === undefined || value === '') return '-';
  if (typeof value === 'number') return value.toLocaleString('ko-KR');
  return String(value);
}

export default function AdminAiOrderAnomaliesPage() {
  const [items, setItems] = useState<AiInsight[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await aiOperationsService.getOrderAnomalies(10);
        if (!mounted) return;
        setItems(response);
      } catch (err) {
        if (!mounted) return;
        setItems([]);
        setError(err instanceof Error ? err.message : 'AI 이상 주문 후보를 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, [reloadKey]);

  return (
    <AdminLayout title="AI 이상 주문 탐지">
      <div className="space-y-5">
        <section className="border border-[#e8eaf0] bg-white p-5">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h1 className="text-lg font-semibold text-[#1a1f2e]">AI 이상 주문 탐지</h1>
              <p className="mt-2 max-w-3xl text-sm leading-6 text-[#6f7a8a]">
                고액 주문, 높은 할인율, 주문/결제 상태 불일치 후보를 표시합니다.
                자동 차단이 아니라 운영자가 직접 확인할 주문을 찾기 위한 참고 지표입니다.
              </p>
            </div>
            <div className="flex gap-2">
              <Link href="/admin/orders">
                <Button variant="outline" size="sm">주문 관리</Button>
              </Link>
              <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)}>
                새로고침
              </Button>
            </div>
          </div>
        </section>

        {loading ? (
          <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#8a9bb5]">
            AI 이상 주문 후보를 불러오는 중입니다.
          </div>
        ) : error ? (
          <div className="border border-[#f1c7c7] bg-white px-5 py-12 text-center">
            <p className="text-sm text-[#c43a3a]">{error}</p>
            <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
              다시 불러오기
            </Button>
          </div>
        ) : (
          <DataTable<AiInsight>
            keyField="id"
            data={items}
            emptyMessage="이상 후보로 표시할 주문이 없습니다."
            columns={[
              { key: 'orderNumber', header: '주문번호', render: (row) => featureText(row.features.orderNumber) },
              {
                key: 'riskLevel',
                header: '탐지 상태',
                render: (row) => (
                  <span className={['inline-flex border px-2 py-1 text-xs font-semibold', RISK_CLASS[row.riskLevel]].join(' ')}>
                    {RISK_LABEL[row.riskLevel]}
                  </span>
                ),
              },
              { key: 'score', header: '위험 점수', render: (row) => `${Math.round(row.score * 100)}점` },
              { key: 'totalPrice', header: '주문금액', render: (row) => formatPrice(Number(row.features.totalPrice ?? 0)) },
              { key: 'discountRate', header: '할인율', render: (row) => `${featureText(row.features.discountRate)}%` },
              { key: 'orderStatus', header: '주문 상태', render: (row) => featureText(row.features.orderStatus) },
              { key: 'paymentStatus', header: '결제 상태', render: (row) => featureText(row.features.paymentStatus) },
              {
                key: 'reason',
                header: '탐지 근거',
                render: (row) => <p className="max-w-[420px] text-sm leading-6 text-[#4b5565]">{row.reason}</p>,
              },
              { key: 'generatedAt', header: '생성 시각', render: (row) => formatDateTime(row.generatedAt) },
            ]}
          />
        )}
      </div>
    </AdminLayout>
  );
}
