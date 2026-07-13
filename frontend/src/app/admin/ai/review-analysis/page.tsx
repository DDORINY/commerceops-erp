'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import DataTable from '@/components/admin/DataTable';
import { formatDateTime } from '@/lib/format';
import { aiOperationsService, type AiInsight, type AiRiskLevel } from '@/lib/services/aiOperationsService';

const RISK_LABEL: Record<AiRiskLevel, string> = {
  LOW: '긍정',
  MEDIUM: '중립',
  HIGH: '부정 확인',
};

const RISK_CLASS: Record<AiRiskLevel, string> = {
  LOW: 'bg-green-50 text-green-700 border-green-200',
  MEDIUM: 'bg-yellow-50 text-yellow-700 border-yellow-200',
  HIGH: 'bg-red-50 text-red-700 border-red-200',
};

function featureText(value: unknown) {
  if (value === null || value === undefined || value === '') return '-';
  return String(value);
}

export default function AdminAiReviewAnalysisPage() {
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
        const response = await aiOperationsService.getReviewAnalyses(10);
        if (!mounted) return;
        setItems(response);
      } catch (err) {
        if (!mounted) return;
        setItems([]);
        setError(err instanceof Error ? err.message : 'AI 리뷰 분석 결과를 불러오지 못했습니다.');
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
    <AdminLayout title="AI 리뷰 분석">
      <div className="space-y-5">
        <section className="border border-[#e8eaf0] bg-white p-5">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h1 className="text-lg font-semibold text-[#1a1f2e]">AI 리뷰 분석</h1>
              <p className="mt-2 max-w-3xl text-sm leading-6 text-[#6f7a8a]">
                리뷰 평점과 마스킹된 본문을 기준으로 감성 후보를 표시합니다.
                자동 숨김이나 제재가 아니라 운영자가 확인할 리뷰를 찾는 참고 지표입니다.
              </p>
            </div>
            <div className="flex gap-2">
              <Link href="/admin/reviews">
                <Button variant="outline" size="sm">리뷰 관리</Button>
              </Link>
              <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)}>
                새로고침
              </Button>
            </div>
          </div>
        </section>

        {loading ? (
          <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#8a9bb5]">
            AI 리뷰 분석 결과를 불러오는 중입니다.
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
            emptyMessage="분석할 리뷰가 없습니다."
            columns={[
              { key: 'product', header: '상품', render: (row) => featureText(row.features.productName) },
              {
                key: 'riskLevel',
                header: '감성 후보',
                render: (row) => (
                  <span className={['inline-flex border px-2 py-1 text-xs font-semibold', RISK_CLASS[row.riskLevel]].join(' ')}>
                    {RISK_LABEL[row.riskLevel]}
                  </span>
                ),
              },
              { key: 'score', header: '감성 점수', render: (row) => `${Math.round(row.score * 100)}점` },
              { key: 'rating', header: '평점', render: (row) => `${featureText(row.features.rating)}점` },
              {
                key: 'content',
                header: '마스킹 본문',
                render: (row) => <p className="max-w-[360px] truncate text-sm text-[#4b5565]">{featureText(row.features.maskedContent)}</p>,
              },
              {
                key: 'reason',
                header: '분석 근거',
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
