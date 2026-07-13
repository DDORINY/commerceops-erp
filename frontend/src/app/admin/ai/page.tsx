'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import AiImageInferenceCard from '@/components/admin/AiImageInferenceCard';
import Button from '@/components/common/Button';
import { formatDateTime } from '@/lib/format';
import {
  aiOperationsService,
  type AiOperationsHealth,
  type AiOperationsOverview,
  type AiRiskLevel,
} from '@/lib/services/aiOperationsService';

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

export default function AdminAiOverviewPage() {
  const [overview, setOverview] = useState<AiOperationsOverview | null>(null);
  const [health, setHealth] = useState<AiOperationsHealth | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [overviewResponse, healthResponse] = await Promise.all([
          aiOperationsService.getOverview(),
          aiOperationsService.getHealth(),
        ]);
        if (!mounted) return;
        setOverview(overviewResponse);
        setHealth(healthResponse);
      } catch (err) {
        if (!mounted) return;
        setOverview(null);
        setHealth(null);
        setError(err instanceof Error ? err.message : 'AI 운영 정보를 불러오지 못했습니다.');
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
    <AdminLayout title="AI 운영">
      <div className="space-y-5">
        <section className="border border-[#e8eaf0] bg-white p-5">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h1 className="text-lg font-semibold text-[#1a1f2e]">AI 운영 개요</h1>
              <p className="mt-2 max-w-3xl text-sm leading-6 text-[#6f7a8a]">
                포트폴리오 데모 데이터셋과 baseline 모델 구조를 기반으로 추천, 예측, 분석, 탐지 화면을 단계별로 연결합니다.
                AI 결과는 자동 실행이 아니라 관리자 의사결정을 돕는 참고 지표로 표시합니다.
              </p>
            </div>
            <div className="flex gap-2">
              <Link href="/admin/settings?section=ai-datasets">
                <Button variant="outline" size="sm">AI 데이터셋</Button>
              </Link>
              <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)}>
                새로고침
              </Button>
            </div>
          </div>
        </section>

        {loading ? (
          <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#8a9bb5]">
            AI 운영 정보를 불러오는 중입니다.
          </div>
        ) : error ? (
          <div className="border border-[#f1c7c7] bg-white px-5 py-12 text-center">
            <p className="text-sm text-[#c43a3a]">{error}</p>
            <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
              다시 불러오기
            </Button>
          </div>
        ) : overview && health ? (
          <>
            <section className="grid gap-4 lg:grid-cols-3">
              <div className="border border-[#e8eaf0] bg-white p-4">
                <p className="text-xs font-semibold text-[#8a9bb5]">데이터셋 상태</p>
                <p className="mt-2 text-base font-semibold text-[#1a1f2e]">{overview.datasetStatus}</p>
              </div>
              <div className="border border-[#e8eaf0] bg-white p-4">
                <p className="text-xs font-semibold text-[#8a9bb5]">모델 상태</p>
                <p className="mt-2 text-base font-semibold text-[#1a1f2e]">{overview.modelStatus}</p>
              </div>
              <div className="border border-[#e8eaf0] bg-white p-4">
                <p className="text-xs font-semibold text-[#8a9bb5]">헬스 체크</p>
                <p className={['mt-2 text-base font-semibold', health.available ? 'text-green-700' : 'text-yellow-700'].join(' ')}>
                  {health.status} · {health.message}
                </p>
              </div>
            </section>

            <section className="border border-[#e8eaf0] bg-white p-5">
              <h2 className="text-base font-semibold text-[#1a1f2e]">활성 예정 모듈</h2>
              <div className="mt-3 flex flex-wrap gap-2">
                {overview.enabledModules.map((module) => (
                  <span key={module} className="border border-[#d9dde7] bg-[#f8f9fb] px-3 py-1 text-xs text-[#4b5565]">
                    {module}
                  </span>
                ))}
              </div>
            </section>

            <section className="border border-[#e8eaf0] bg-white p-5">
              <div className="flex items-center justify-between gap-4">
                <h2 className="text-base font-semibold text-[#1a1f2e]">AI 운영 하이라이트</h2>
                <span className="text-xs text-[#8a9bb5]">생성 시각: {formatDateTime(overview.generatedAt)}</span>
              </div>
              <div className="mt-4 grid gap-4 xl:grid-cols-3">
                {overview.highlights.map((item) => (
                  <article key={item.id} className="border border-[#e8eaf0] p-4">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-sm font-semibold text-[#1a1f2e]">{item.title}</p>
                        <p className="mt-1 text-xs text-[#8a9bb5]">{item.modelName}</p>
                      </div>
                      <span className={['shrink-0 border px-2 py-1 text-xs font-semibold', RISK_CLASS[item.riskLevel]].join(' ')}>
                        {RISK_LABEL[item.riskLevel]}
                      </span>
                    </div>
                    <p className="mt-3 text-sm leading-6 text-[#4b5565]">{item.reason}</p>
                    <div className="mt-4 flex items-center justify-between border-t border-[#eef0f4] pt-3 text-xs text-[#8a9bb5]">
                      <span>점수 {Math.round(item.score * 100)}점</span>
                      <span>{item.targetType}</span>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          </>
        ) : (
          <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#8a9bb5]">
            표시할 AI 운영 정보가 없습니다.
          </div>
        )}
        <AiImageInferenceCard />
      </div>
    </AdminLayout>
  );
}
