'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import { formatDateTime } from '@/lib/format';
import { aiOperationsService, type AiReport } from '@/lib/services/aiOperationsService';

export default function AdminAiReportsPage() {
  const [reports, setReports] = useState<AiReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await aiOperationsService.getReports();
        if (!mounted) return;
        setReports(response);
      } catch (err) {
        if (!mounted) return;
        setReports([]);
        setError(err instanceof Error ? err.message : 'AI 리포트를 불러오지 못했습니다.');
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
    <AdminLayout title="AI 리포트">
      <div className="space-y-5">
        <section className="border border-[#e8eaf0] bg-white p-5">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h1 className="text-lg font-semibold text-[#1a1f2e]">AI 리포트와 근거 설명</h1>
              <p className="mt-2 max-w-3xl text-sm leading-6 text-[#6f7a8a]">
                v0.9 AI 운영 화면에서 사용하는 점수와 근거의 출처, 해석 기준, 주의 사항을 정리합니다.
                리포트는 자동 판단 결과가 아니라 포트폴리오 데모를 설명하기 위한 운영 문서형 카드입니다.
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
            AI 리포트를 불러오는 중입니다.
          </div>
        ) : error ? (
          <div className="border border-[#f1c7c7] bg-white px-5 py-12 text-center">
            <p className="text-sm text-[#c43a3a]">{error}</p>
            <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
              다시 불러오기
            </Button>
          </div>
        ) : reports.length === 0 ? (
          <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#8a9bb5]">
            표시할 AI 리포트가 없습니다.
          </div>
        ) : (
          <section className="grid gap-4 xl:grid-cols-2">
            {reports.map((report) => (
              <article key={report.id} className="border border-[#e8eaf0] bg-white p-5">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-xs font-semibold text-[#8a9bb5]">{report.relatedModule}</p>
                    <h2 className="mt-1 text-base font-semibold text-[#1a1f2e]">{report.title}</h2>
                  </div>
                  <span className="shrink-0 border border-[#d9dde7] bg-[#f8f9fb] px-2 py-1 text-xs text-[#4b5565]">
                    {report.modelName}
                  </span>
                </div>
                <p className="mt-3 text-sm leading-6 text-[#4b5565]">{report.summary}</p>
                <div className="mt-4 grid gap-4 md:grid-cols-2">
                  <div>
                    <p className="text-xs font-semibold text-[#8a9bb5]">근거 데이터</p>
                    <ul className="mt-2 space-y-1 text-sm text-[#4b5565]">
                      {report.evidenceSources.map((item) => (
                        <li key={item}>- {item}</li>
                      ))}
                    </ul>
                  </div>
                  <div>
                    <p className="text-xs font-semibold text-[#8a9bb5]">해석 기준</p>
                    <ul className="mt-2 space-y-1 text-sm text-[#4b5565]">
                      {report.interpretationGuide.map((item) => (
                        <li key={item}>- {item}</li>
                      ))}
                    </ul>
                  </div>
                </div>
                <p className="mt-4 border-t border-[#eef0f4] pt-3 text-xs text-[#8a9bb5]">
                  생성 시각: {formatDateTime(report.generatedAt)}
                </p>
              </article>
            ))}
          </section>
        )}
      </div>
    </AdminLayout>
  );
}
