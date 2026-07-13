'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import DataTable from '@/components/admin/DataTable';
import { formatDateTime, formatPrice } from '@/lib/format';
import { aiOperationsService, type AiInsight, type AiRiskLevel } from '@/lib/services/aiOperationsService';

const RISK_LABEL: Record<AiRiskLevel, string> = {
  LOW: '낮음',
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

function RiskBadge({ level }: { level: AiRiskLevel }) {
  return (
    <span className={['inline-flex border px-2 py-1 text-xs font-semibold', RISK_CLASS[level]].join(' ')}>
      {RISK_LABEL[level]}
    </span>
  );
}

export default function AdminAiRiskAlertsPage() {
  const [inventoryAlerts, setInventoryAlerts] = useState<AiInsight[]>([]);
  const [settlementAlerts, setSettlementAlerts] = useState<AiInsight[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [inventory, settlement] = await Promise.all([
          aiOperationsService.getInventoryRiskAlerts(10),
          aiOperationsService.getSettlementRiskAlerts(10),
        ]);
        if (!mounted) return;
        setInventoryAlerts(inventory);
        setSettlementAlerts(settlement);
      } catch (err) {
        if (!mounted) return;
        setInventoryAlerts([]);
        setSettlementAlerts([]);
        setError(err instanceof Error ? err.message : 'AI 리스크 알림을 불러오지 못했습니다.');
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
    <AdminLayout title="AI 리스크 알림">
      <div className="space-y-5">
        <section className="border border-[#e8eaf0] bg-white p-5">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h1 className="text-lg font-semibold text-[#1a1f2e]">AI 리스크 알림</h1>
              <p className="mt-2 max-w-3xl text-sm leading-6 text-[#6f7a8a]">
                재고 부족과 정산 확인 필요 후보를 한 화면에서 확인합니다.
                자동 입고나 정산 마감이 아니라 운영자가 먼저 볼 리스크를 정리하는 참고 지표입니다.
              </p>
            </div>
            <div className="flex gap-2">
              <Link href="/admin/inventory">
                <Button variant="outline" size="sm">재고 관리</Button>
              </Link>
              <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)}>
                새로고침
              </Button>
            </div>
          </div>
        </section>

        {loading ? (
          <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#8a9bb5]">
            AI 리스크 알림을 불러오는 중입니다.
          </div>
        ) : error ? (
          <div className="border border-[#f1c7c7] bg-white px-5 py-12 text-center">
            <p className="text-sm text-[#c43a3a]">{error}</p>
            <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
              다시 불러오기
            </Button>
          </div>
        ) : (
          <>
            <section className="space-y-3">
              <h2 className="text-base font-semibold text-[#1a1f2e]">재고 리스크</h2>
              <DataTable<AiInsight>
                keyField="id"
                data={inventoryAlerts}
                emptyMessage="재고 리스크 알림이 없습니다."
                columns={[
                  { key: 'productName', header: '상품', render: (row) => featureText(row.features.productName) },
                  { key: 'riskLevel', header: '리스크', render: (row) => <RiskBadge level={row.riskLevel} /> },
                  { key: 'score', header: '점수', render: (row) => `${Math.round(row.score * 100)}점` },
                  { key: 'stockQuantity', header: '현재 재고', render: (row) => `${featureText(row.features.stockQuantity)}개` },
                  { key: 'safetyStockQuantity', header: '안전재고', render: (row) => `${featureText(row.features.safetyStockQuantity)}개` },
                  { key: 'reason', header: '근거', render: (row) => <p className="max-w-[420px] text-sm leading-6 text-[#4b5565]">{row.reason}</p> },
                  { key: 'generatedAt', header: '생성 시각', render: (row) => formatDateTime(row.generatedAt) },
                ]}
              />
            </section>

            <section className="space-y-3">
              <h2 className="text-base font-semibold text-[#1a1f2e]">정산 리스크</h2>
              <DataTable<AiInsight>
                keyField="id"
                data={settlementAlerts}
                emptyMessage="정산 리스크 알림이 없습니다."
                columns={[
                  { key: 'batchNumber', header: '정산 배치', render: (row) => featureText(row.features.batchNumber) },
                  { key: 'riskLevel', header: '리스크', render: (row) => <RiskBadge level={row.riskLevel} /> },
                  { key: 'score', header: '점수', render: (row) => `${Math.round(row.score * 100)}점` },
                  { key: 'status', header: '상태', render: (row) => featureText(row.features.status) },
                  { key: 'netAmount', header: '순정산금액', render: (row) => formatPrice(Number(row.features.netAmount ?? 0)) },
                  { key: 'reason', header: '근거', render: (row) => <p className="max-w-[420px] text-sm leading-6 text-[#4b5565]">{row.reason}</p> },
                  { key: 'generatedAt', header: '생성 시각', render: (row) => formatDateTime(row.generatedAt) },
                ]}
              />
            </section>
          </>
        )}
      </div>
    </AdminLayout>
  );
}
