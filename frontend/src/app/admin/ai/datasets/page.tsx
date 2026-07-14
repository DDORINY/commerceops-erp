'use client';

import { useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import {
  aiOperationsService,
  type AiDatasetCatalogItem,
  type AiDatasetExport,
  type AiDatasetKey,
} from '@/lib/services/aiOperationsService';

function downloadJson(dataset: AiDatasetExport) {
  const blob = new Blob([JSON.stringify(dataset, null, 2)], { type: 'application/json;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = `${dataset.key.toLowerCase()}-${dataset.exportedAt.slice(0, 10)}.json`;
  anchor.click();
  URL.revokeObjectURL(url);
}

function cellText(value: unknown) {
  if (value == null) return '-';
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
}

export default function AdminAiDatasetsPage() {
  const [catalog, setCatalog] = useState<AiDatasetCatalogItem[]>([]);
  const [selectedKey, setSelectedKey] = useState<AiDatasetKey | null>(null);
  const [dataset, setDataset] = useState<AiDatasetExport | null>(null);
  const [limit, setLimit] = useState(100);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;
    aiOperationsService.getDatasetCatalog()
      .then((items) => {
        if (!mounted) return;
        setCatalog(items);
        setSelectedKey(items[0]?.key ?? null);
      })
      .catch((err) => {
        if (mounted) setError(err instanceof Error ? err.message : '데이터셋 목록을 불러오지 못했습니다.');
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => { mounted = false; };
  }, []);

  const exportSelected = async () => {
    if (!selectedKey) return;
    setExporting(true);
    setError('');
    try {
      setDataset(await aiOperationsService.exportDataset(selectedKey, limit));
    } catch (err) {
      setError(err instanceof Error ? err.message : '데이터셋 내보내기에 실패했습니다.');
    } finally {
      setExporting(false);
    }
  };

  return (
    <AdminLayout title="AI 데이터셋">
      <div className="space-y-5">
        <section className="border border-[#e8eaf0] bg-white p-5">
          <h2 className="text-base font-semibold text-[#1a1f2e]">학습·분석 데이터셋</h2>
          <p className="mt-2 text-sm text-[#6f7a8a]">운영 데이터를 개인정보 마스킹 기준으로 조회하고 JSON으로 내보냅니다.</p>
        </section>

        {error && <p className="border border-[#f0c7c7] bg-[#fff5f5] p-3 text-sm text-[#c44242]">{error}</p>}

        {loading ? (
          <div className="border border-[#e8eaf0] bg-white p-10 text-center text-sm text-[#8a9bb5]">데이터셋 목록을 불러오는 중입니다.</div>
        ) : (
          <div className="grid gap-5 xl:grid-cols-[360px_minmax(0,1fr)]">
            <section className="space-y-2">
              {catalog.map((item) => (
                <button
                  key={item.key}
                  type="button"
                  onClick={() => { setSelectedKey(item.key); setDataset(null); }}
                  className={`w-full border p-4 text-left transition-colors ${selectedKey === item.key ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white' : 'border-[#e8eaf0] bg-white hover:border-[#aeb8c8]'}`}
                >
                  <p className="text-sm font-semibold">{item.label}</p>
                  <p className={`mt-1 text-xs leading-5 ${selectedKey === item.key ? 'text-white/70' : 'text-[#6f7a8a]'}`}>{item.description}</p>
                  <p className={`mt-2 text-[11px] ${selectedKey === item.key ? 'text-[#f3a6b8]' : 'text-[#9aa3b1]'}`}>{item.fields.length}개 필드</p>
                </button>
              ))}
            </section>

            <section className="min-w-0 border border-[#e8eaf0] bg-white p-4 sm:p-5">
              <div className="flex flex-wrap items-center gap-3">
                <label className="text-sm text-[#566171]">
                  최대 행 수
                  <select value={limit} onChange={(e) => setLimit(Number(e.target.value))} className="ml-2 border border-[#dfe3ea] bg-white px-3 py-2 text-sm">
                    {[10, 50, 100, 250, 500].map((value) => <option key={value} value={value}>{value}</option>)}
                  </select>
                </label>
                <Button onClick={exportSelected} disabled={!selectedKey || exporting}>{exporting ? '조회 중' : '데이터 조회'}</Button>
                {dataset && <Button variant="outline" onClick={() => downloadJson(dataset)}>JSON 다운로드</Button>}
              </div>

              {!dataset ? (
                <p className="py-16 text-center text-sm text-[#8a9bb5]">데이터셋을 선택하고 데이터를 조회해주세요.</p>
              ) : (
                <div className="mt-5 space-y-3">
                  <div className="flex flex-wrap items-center gap-3 text-xs text-[#6f7a8a]">
                    <span>조회 결과 <strong className="text-[#1a1f2e]">{dataset.rowCount}</strong>건</span>
                    <span>{dataset.privacyMasked ? '개인정보 마스킹 적용' : '마스킹 미적용'}</span>
                  </div>
                  <div className="max-w-full overflow-x-auto border border-[#edf0f5]">
                    <table className="min-w-max text-xs">
                      <thead className="bg-[#f8f9fb] text-[#6f7a8a]">
                        <tr>{dataset.fields.map((field) => <th key={field} className="whitespace-nowrap px-3 py-2 text-left font-semibold">{field}</th>)}</tr>
                      </thead>
                      <tbody className="divide-y divide-[#edf0f5]">
                        {dataset.rows.map((row, index) => (
                          <tr key={index}>{dataset.fields.map((field) => <td key={field} className="max-w-[240px] truncate px-3 py-2 text-[#333]" title={cellText(row[field])}>{cellText(row[field])}</td>)}</tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </section>
          </div>
        )}
      </div>
    </AdminLayout>
  );
}
