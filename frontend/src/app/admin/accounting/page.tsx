'use client';

import { useState, useEffect, useCallback } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import StatCard from '@/components/admin/StatCard';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import {
  accountingService,
  type ApiAccountingEntry,
  type ApiAccountingSummary,
} from '@/lib/services/accountingService';
import {
  formatPrice,
  formatDateTime,
  ACCOUNTING_TYPE_LABEL,
  ACCOUNTING_TYPE_COLOR,
  downloadCsv,
} from '@/lib/format';

type TypeFilter = 'ALL' | 'SALE' | 'REFUND' | 'INBOUND';

const TYPE_FILTERS: { value: TypeFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'SALE', label: '매출' },
  { value: 'REFUND', label: '환불' },
  { value: 'INBOUND', label: '매입' },
];

const PAGE_SIZE = 20;

export default function AdminAccountingPage() {
  const [summary, setSummary] = useState<ApiAccountingSummary | null>(null);
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('ALL');
  const [page, setPage] = useState(1);
  const [entries, setEntries] = useState<ApiAccountingEntry[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    accountingService.getSummary().then(setSummary).catch(() => {});
  }, []);

  const fetchEntries = useCallback(() => {
    accountingService
      .getEntries(typeFilter, page - 1, PAGE_SIZE)
      .then((res) => {
        setEntries(res.content);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements);
      })
      .catch(() => setEntries([]))
      .finally(() => setLoading(false));
  }, [typeFilter, page]);

  useEffect(() => {
    fetchEntries();
  }, [fetchEntries]);

  return (
    <AdminLayout title="회계 관리">
      {/* 요약 카드 */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
        <StatCard
          title="총 매출"
          value={formatPrice(summary?.totalSales ?? 0)}
          iconBgColor="bg-blue-100"
          icon={
            <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
          }
        />
        <StatCard
          title="총 환불"
          value={formatPrice(summary?.totalRefunds ?? 0)}
          iconBgColor="bg-red-100"
          icon={
            <svg className="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
            </svg>
          }
        />
        <StatCard
          title="총 매입"
          value={formatPrice(summary?.totalInbound ?? 0)}
          iconBgColor="bg-purple-100"
          icon={
            <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
            </svg>
          }
        />
        <StatCard
          title="순매출"
          value={formatPrice(summary?.netSales ?? 0)}
          subtitle="매출 − 환불"
          iconBgColor="bg-green-100"
          icon={
            <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          }
        />
      </div>

      {/* 전표 목록 */}
      <div className="flex flex-wrap items-center gap-2 mb-4">
        {TYPE_FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => { setTypeFilter(f.value); setPage(1); }}
            className={[
              'px-4 py-1.5 text-xs font-medium border transition-colors',
              typeFilter === f.value
                ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white'
                : 'border-[#e8eaf0] text-[#8a9bb5] hover:border-[#1a1f2e] hover:text-[#1a1f2e] bg-white',
            ].join(' ')}
          >
            {f.label}
          </button>
        ))}
        <button
          onClick={() => {
            downloadCsv(
              `accounting_${new Date().toISOString().slice(0,10)}.csv`,
              ['ID', '구분', '금액', '설명', '참조ID', '일시'],
              entries.map((e) => [e.entryId, ACCOUNTING_TYPE_LABEL[e.type] ?? e.type, e.amount, e.description, e.referenceId ?? '', formatDateTime(e.createdAt)])
            );
          }}
          className="ml-auto px-3 py-1.5 text-xs font-medium border border-[#e8eaf0] text-[#8a9bb5] hover:border-[#1a1f2e] hover:text-[#1a1f2e] bg-white transition-colors"
        >
          CSV 다운로드
        </button>
      </div>

      {loading ? (
        <div className="py-12 text-center text-[#bbb] text-sm">로딩 중...</div>
      ) : (
        <DataTable<ApiAccountingEntry>
          keyField="entryId"
          data={entries}
          columns={[
            { key: 'entryId', header: 'ID', render: (row) => <span className="text-xs text-[#aaa]">#{row.entryId}</span> },
            {
              key: 'type',
              header: '구분',
              render: (row) => (
                <span className={`text-xs font-medium px-2 py-0.5 ${ACCOUNTING_TYPE_COLOR[row.type] ?? ''}`}>
                  {ACCOUNTING_TYPE_LABEL[row.type] ?? row.type}
                </span>
              ),
            },
            {
              key: 'amount',
              header: '금액',
              render: (row) => (
                <span className={`font-semibold text-sm tabular-nums ${
                  row.type === 'REFUND' ? 'text-red-500' : 'text-[#1a1f2e]'
                }`}>
                  {row.type === 'REFUND' ? '−' : '+'}{formatPrice(row.amount)}
                </span>
              ),
            },
            { key: 'description', header: '내용', render: (row) => <span className="text-xs text-[#555]">{row.description}</span> },
            {
              key: 'referenceId',
              header: '참조번호',
              render: (row) => <span className="text-xs font-mono text-[#aaa]">{row.referenceId ?? '—'}</span>,
            },
            {
              key: 'createdAt',
              header: '일시',
              render: (row) => <span className="text-xs">{formatDateTime(row.createdAt)}</span>,
            },
          ]}
        />
      )}

      <div className="mt-2 text-xs text-[#aaa]">총 {totalElements}건</div>
      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
    </AdminLayout>
  );
}
