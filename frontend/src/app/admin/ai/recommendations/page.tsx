'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import DataTable from '@/components/admin/DataTable';
import { formatDateTime, formatPrice } from '@/lib/format';
import { aiOperationsService, type AiInsight, type AiRiskLevel } from '@/lib/services/aiOperationsService';

const RISK_LABEL: Record<AiRiskLevel, string> = {
  LOW: '추천 가능',
  MEDIUM: '재고 확인',
  HIGH: '추천 보류',
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

export default function AdminAiProductRecommendationsPage() {
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
        const response = await aiOperationsService.getProductRecommendations(10);
        if (!mounted) return;
        setItems(response);
      } catch (err) {
        if (!mounted) return;
        setItems([]);
        setError(err instanceof Error ? err.message : 'AI 상품 추천 후보를 불러오지 못했습니다.');
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
    <AdminLayout title="AI 상품 추천">
      <div className="space-y-5">
        <section className="border border-[#e8eaf0] bg-white p-5">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h1 className="text-lg font-semibold text-[#1a1f2e]">AI 상품 추천 후보</h1>
              <p className="mt-2 max-w-3xl text-sm leading-6 text-[#6f7a8a]">
                노출 중인 판매 가능 상품을 대상으로 재고, 태그, 검색 키워드, 이미지 여부를 점수화한 추천 후보입니다.
                실제 개인화 추천이나 자동 전시는 적용하지 않고 관리자 검토용 참고 지표로만 사용합니다.
              </p>
            </div>
            <div className="flex gap-2">
              <Link href="/admin/ai">
                <Button variant="outline" size="sm">AI 운영 개요</Button>
              </Link>
              <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)}>
                새로고침
              </Button>
            </div>
          </div>
        </section>

        {loading ? (
          <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#8a9bb5]">
            AI 상품 추천 후보를 불러오는 중입니다.
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
            emptyMessage="추천 후보로 표시할 상품이 없습니다."
            columns={[
              {
                key: 'title',
                header: '상품',
                render: (row) => (
                  <div>
                    <p className="font-medium text-[#1a1f2e]">{featureText(row.features.productName)}</p>
                    <p className="text-xs text-[#8a9bb5]">{featureText(row.features.brand)}</p>
                  </div>
                ),
              },
              {
                key: 'score',
                header: '추천 점수',
                render: (row) => `${Math.round(row.score * 100)}점`,
              },
              {
                key: 'riskLevel',
                header: '상태',
                render: (row) => (
                  <span className={['inline-flex border px-2 py-1 text-xs font-semibold', RISK_CLASS[row.riskLevel]].join(' ')}>
                    {RISK_LABEL[row.riskLevel]}
                  </span>
                ),
              },
              {
                key: 'price',
                header: '판매가',
                render: (row) => formatPrice(Number(row.features.price ?? 0)),
              },
              {
                key: 'stock',
                header: '재고',
                render: (row) => `${featureText(row.features.stockQuantity)}개`,
              },
              {
                key: 'reason',
                header: '추천 근거',
                render: (row) => <p className="max-w-[420px] text-sm leading-6 text-[#4b5565]">{row.reason}</p>,
              },
              {
                key: 'generatedAt',
                header: '생성 시각',
                render: (row) => formatDateTime(row.generatedAt),
              },
            ]}
          />
        )}
      </div>
    </AdminLayout>
  );
}
