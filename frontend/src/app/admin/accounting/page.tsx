'use client';

import { useCallback, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import StatCard from '@/components/admin/StatCard';
import {
  accountingService,
  type ApiAccountingConsistencyIssue,
  type ApiAccountingConsistencyReport,
  type ApiAccountingEntry,
  type ApiAccountingEntryType,
  type ApiAccountingLedger,
  type ApiAccountingSummary,
  type ApiAccountingTransaction,
  type ApiSettlementBatch,
  type ApiShippingCostEntry,
} from '@/lib/services/accountingService';
import { ACCOUNTING_TYPE_COLOR, ACCOUNTING_TYPE_LABEL, downloadCsv, formatDateTime, formatPrice } from '@/lib/format';

type TypeFilter = 'ALL' | ApiAccountingEntryType;

const PAGE_SIZE = 20;
const PREVIEW_SIZE = 8;

const TYPE_FILTERS: { value: TypeFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'SALE', label: '매출' },
  { value: 'REFUND', label: '환불' },
  { value: 'INBOUND', label: '입고' },
];

const LEDGER_STATUS_LABEL: Record<ApiAccountingLedger['status'], string> = {
  OPEN: '진행 중',
  CLOSED: '마감',
  CANCELLED: '취소',
};

const TRANSACTION_TYPE_LABEL: Record<ApiAccountingTransaction['type'], string> = {
  SALES: '매출',
  REFUND: '환불',
  SHIPPING_REVENUE: '배송비 매출',
  SHIPPING_COST: '택배비 비용',
  RETURN_FEE: '반품 배송비',
  ADJUSTMENT: '조정',
  SETTLEMENT: '정산',
};

const TRANSACTION_DIRECTION_LABEL: Record<ApiAccountingTransaction['direction'], string> = {
  DEBIT: '차변',
  CREDIT: '대변',
  INCOME: '수익',
  EXPENSE: '비용',
};

const REFERENCE_TYPE_LABEL: Record<ApiAccountingTransaction['referenceType'] | ApiAccountingConsistencyIssue['sourceType'], string> = {
  ORDER: '주문',
  PAYMENT: '결제',
  REFUND: '환불',
  RETURN: '반품',
  SHIPMENT: '배송',
  OUTBOUND_ORDER: '출고',
  SHIPPING_METHOD: '배송 방법',
  SETTLEMENT_BATCH: '정산 배치',
};

const SETTLEMENT_STATUS_LABEL: Record<ApiSettlementBatch['status'], string> = {
  DRAFT: '작성 중',
  CONFIRMED: '확정',
  CLOSED: '마감',
  CANCELLED: '취소',
};

const CONSISTENCY_ISSUE_LABEL: Record<string, string> = {
  MISSING_REVENUE: '매출 누락',
  MISSING_PAYMENT_REFUND: '결제 환불 누락',
  MISSING_RETURN_REFUND: '반품 환불 누락',
  MISSING_RETURN_FEE: '반품 배송비 누락',
  MISSING_SHIPPING_COST: '택배비 비용 누락',
};

export default function AdminAccountingPage() {
  const [summary, setSummary] = useState<ApiAccountingSummary | null>(null);
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('ALL');
  const [page, setPage] = useState(1);
  const [entries, setEntries] = useState<ApiAccountingEntry[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [summaryError, setSummaryError] = useState<string | null>(null);
  const [entriesError, setEntriesError] = useState<string | null>(null);
  const [ledgers, setLedgers] = useState<ApiAccountingLedger[]>([]);
  const [transactions, setTransactions] = useState<ApiAccountingTransaction[]>([]);
  const [shippingCosts, setShippingCosts] = useState<ApiShippingCostEntry[]>([]);
  const [settlements, setSettlements] = useState<ApiSettlementBatch[]>([]);
  const [consistencyReport, setConsistencyReport] = useState<ApiAccountingConsistencyReport | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);

  useEffect(() => {
    accountingService
      .getSummary()
      .then((data) => {
        setSummary(data);
        setSummaryError(null);
      })
      .catch((err) => {
        setSummary(null);
        setSummaryError(err instanceof Error ? err.message : '회계 요약을 불러오지 못했습니다.');
      });
  }, []);

  const fetchEntries = useCallback(() => {
    accountingService
      .getEntries(typeFilter, page - 1, PAGE_SIZE)
      .then((res) => {
        setEntries(res.content);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements);
        setEntriesError(null);
      })
      .catch((err) => {
        setEntries([]);
        setTotalPages(1);
        setTotalElements(0);
        setEntriesError(err instanceof Error ? err.message : '회계 내역을 불러오지 못했습니다.');
      })
      .finally(() => setLoading(false));
  }, [typeFilter, page]);

  useEffect(() => {
    fetchEntries();
  }, [fetchEntries]);

  useEffect(() => {
    Promise.all([
      accountingService.getLedgers({ size: PREVIEW_SIZE }),
      accountingService.getTransactions({ size: PREVIEW_SIZE }),
      accountingService.getShippingCosts(0, PREVIEW_SIZE),
      accountingService.getSettlementBatches({ size: PREVIEW_SIZE }),
      accountingService.getConsistencyReport(PREVIEW_SIZE),
    ])
      .then(([ledgerRes, transactionRes, shippingCostRes, settlementRes, consistencyRes]) => {
        setLedgers(ledgerRes.content);
        setTransactions(transactionRes.content);
        setShippingCosts(shippingCostRes.content);
        setSettlements(settlementRes.content);
        setConsistencyReport(consistencyRes);
        setPreviewError(null);
      })
      .catch((err) => {
        setLedgers([]);
        setTransactions([]);
        setShippingCosts([]);
        setSettlements([]);
        setConsistencyReport(null);
        setPreviewError(err instanceof Error ? err.message : '회계 리포트 데이터를 불러오지 못했습니다.');
      });
  }, []);

  const handleTypeFilterChange = (value: TypeFilter) => {
    setTypeFilter(value);
    setPage(1);
    setLoading(true);
    setEntriesError(null);
  };

  const handlePageChange = (nextPage: number) => {
    setPage(nextPage);
    setLoading(true);
    setEntriesError(null);
  };

  return (
    <AdminLayout title="회계 관리">
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
        <StatCard title="총 매출" value={formatPrice(summary?.totalSales ?? 0)} iconBgColor="bg-blue-100" icon={<span className="text-blue-600 text-sm font-bold">매</span>} />
        <StatCard title="총 환불" value={formatPrice(summary?.totalRefunds ?? 0)} iconBgColor="bg-red-100" icon={<span className="text-red-500 text-sm font-bold">환</span>} />
        <StatCard title="총 입고 금액" value={formatPrice(summary?.totalInbound ?? 0)} iconBgColor="bg-purple-100" icon={<span className="text-purple-600 text-sm font-bold">입</span>} />
        <StatCard title="순매출" value={formatPrice(summary?.netSales ?? 0)} subtitle="매출 - 환불" iconBgColor="bg-green-100" icon={<span className="text-green-600 text-sm font-bold">순</span>} />
      </div>

      {summaryError && <StatusMessage tone="error" message={summaryError} />}
      {entriesError && <StatusMessage tone="error" message={entriesError} />}

      <section className="mb-10">
        <div className="flex flex-wrap items-center gap-2 mb-4">
          {TYPE_FILTERS.map((filter) => (
            <button
              key={filter.value}
              onClick={() => handleTypeFilterChange(filter.value)}
              className={[
                'px-4 py-1.5 text-xs font-medium border transition-colors',
                typeFilter === filter.value
                  ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white'
                  : 'border-[#e8eaf0] text-[#8a9bb5] hover:border-[#1a1f2e] hover:text-[#1a1f2e] bg-white',
              ].join(' ')}
            >
              {filter.label}
            </button>
          ))}
          <button
            disabled={entries.length === 0}
            onClick={() => {
              downloadCsv(
                `accounting_${new Date().toISOString().slice(0, 10)}.csv`,
                ['ID', '구분', '금액', '내용', '참조ID', '일시'],
                entries.map((entry) => [
                  entry.entryId,
                  ACCOUNTING_TYPE_LABEL[entry.type] ?? entry.type,
                  entry.amount,
                  entry.description,
                  entry.referenceId ?? '',
                  formatDateTime(entry.createdAt),
                ]),
              );
            }}
            className="ml-auto px-3 py-1.5 text-xs font-medium border border-[#e8eaf0] text-[#8a9bb5] hover:border-[#1a1f2e] hover:text-[#1a1f2e] bg-white transition-colors disabled:cursor-not-allowed disabled:opacity-50"
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
            emptyMessage="회계 내역이 없습니다."
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
                  <span className={`font-semibold text-sm tabular-nums ${row.type === 'REFUND' ? 'text-red-500' : 'text-[#1a1f2e]'}`}>
                    {row.type === 'REFUND' ? '-' : '+'}
                    {formatPrice(row.amount)}
                  </span>
                ),
              },
              { key: 'description', header: '내용', render: (row) => <span className="text-xs text-[#555]">{row.description}</span> },
              { key: 'referenceId', header: '참조번호', render: (row) => <span className="text-xs font-mono text-[#aaa]">{row.referenceId ?? '-'}</span> },
              { key: 'createdAt', header: '일시', render: (row) => <span className="text-xs">{formatDateTime(row.createdAt)}</span> },
            ]}
          />
        )}
        <div className="mt-2 text-xs text-[#aaa]">총 {totalElements}건</div>
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={handlePageChange} />
      </section>

      {previewError && <StatusMessage tone="error" message={previewError} />}

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
        <ReportSection title="회계 정합성 점검" description="원천 데이터에는 있지만 회계 거래가 아직 없는 후보를 확인합니다.">
          <div className="mb-3 grid grid-cols-2 md:grid-cols-5 gap-2">
            <MiniMetric label="매출" value={consistencyReport?.missingRevenueCount ?? 0} />
            <MiniMetric label="결제 환불" value={consistencyReport?.missingRefundCount ?? 0} />
            <MiniMetric label="반품 환불" value={consistencyReport?.missingReturnRefundCount ?? 0} />
            <MiniMetric label="반품 배송비" value={consistencyReport?.missingReturnFeeCount ?? 0} />
            <MiniMetric label="택배비" value={consistencyReport?.missingShippingCostCount ?? 0} />
          </div>
          <DataTable<ApiAccountingConsistencyIssue>
            keyField="sourceId"
            data={consistencyReport?.issues ?? []}
            emptyMessage="회계 정합성 이슈가 없습니다."
            columns={[
              { key: 'issueType', header: '이슈', render: (row) => <span className="text-xs font-semibold text-red-500">{CONSISTENCY_ISSUE_LABEL[row.issueType] ?? row.issueType}</span> },
              { key: 'source', header: '원천', render: (row) => <span className="text-xs">{REFERENCE_TYPE_LABEL[row.sourceType] ?? row.sourceType} #{row.sourceId}</span> },
              { key: 'sourceNumber', header: '번호', render: (row) => <span className="text-xs font-mono">{row.sourceNumber}</span> },
              { key: 'expectedTransactionType', header: '예상 거래', render: (row) => <span className="text-xs">{TRANSACTION_TYPE_LABEL[row.expectedTransactionType] ?? row.expectedTransactionType}</span> },
              { key: 'expectedAmount', header: '금액', render: (row) => <span className="text-xs font-semibold">{row.expectedAmount == null ? '-' : formatPrice(row.expectedAmount)}</span> },
              { key: 'message', header: '내용', render: (row) => <span className="text-xs">{row.message}</span> },
            ]}
          />
        </ReportSection>

        <ReportSection title="회계 원장" description="기간별 원장과 마감 상태를 확인합니다.">
          <DataTable<ApiAccountingLedger>
            keyField="ledgerId"
            data={ledgers}
            emptyMessage="회계 원장이 없습니다."
            columns={[
              { key: 'ledgerNumber', header: '원장 번호', render: (row) => <span className="text-xs font-mono">{row.ledgerNumber}</span> },
              { key: 'period', header: '기간', render: (row) => <span className="text-xs">{row.period}</span> },
              { key: 'status', header: '상태', render: (row) => <span className="text-xs">{LEDGER_STATUS_LABEL[row.status] ?? row.status}</span> },
              { key: 'closedAt', header: '마감일', render: (row) => <span className="text-xs">{row.closedAt ? formatDateTime(row.closedAt) : '-'}</span> },
            ]}
          />
        </ReportSection>

        <ReportSection title="최근 회계 거래" description="매출, 환불, 배송비, 정산 거래를 함께 확인합니다.">
          <DataTable<ApiAccountingTransaction>
            keyField="transactionId"
            data={transactions}
            emptyMessage="회계 거래가 없습니다."
            columns={[
              { key: 'transactionNumber', header: '거래 번호', render: (row) => <span className="text-xs font-mono">{row.transactionNumber}</span> },
              { key: 'type', header: '유형', render: (row) => <span className="text-xs">{TRANSACTION_TYPE_LABEL[row.type] ?? row.type}</span> },
              { key: 'amount', header: '금액', render: (row) => <span className="text-xs font-semibold">{formatPrice(row.amount)}</span> },
              {
                key: 'reference',
                header: '참조',
                render: (row) => <span className="text-xs">{REFERENCE_TYPE_LABEL[row.referenceType] ?? row.referenceType} #{row.referenceId}</span>,
              },
              { key: 'direction', header: '방향', render: (row) => <span className="text-xs">{TRANSACTION_DIRECTION_LABEL[row.direction] ?? row.direction}</span> },
            ]}
          />
        </ReportSection>

        <ReportSection title="택배비 비용" description="배송 방법 기본비 기준의 택배비 매입 후보입니다.">
          <DataTable<ApiShippingCostEntry>
            keyField="id"
            data={shippingCosts}
            emptyMessage="택배비 비용 항목이 없습니다."
            columns={[
              { key: 'orderNumber', header: '주문번호', render: (row) => <span className="text-xs font-mono">{row.orderNumber}</span> },
              { key: 'carrierName', header: '택배사', render: (row) => <span className="text-xs">{row.carrierName ?? '-'}</span> },
              { key: 'shippingMethodName', header: '배송 방법', render: (row) => <span className="text-xs">{row.shippingMethodName ?? '-'}</span> },
              { key: 'costAmount', header: '비용', render: (row) => <span className="text-xs font-semibold">{formatPrice(row.costAmount)}</span> },
              { key: 'marginAmount', header: '차액', render: (row) => <span className="text-xs">{formatPrice(row.marginAmount)}</span> },
            ]}
          />
        </ReportSection>

        <ReportSection title="정산 배치" description="기간별 정산 집계와 마감 상태입니다.">
          <DataTable<ApiSettlementBatch>
            keyField="id"
            data={settlements}
            emptyMessage="정산 배치가 없습니다."
            columns={[
              { key: 'batchNumber', header: '배치 번호', render: (row) => <span className="text-xs font-mono">{row.batchNumber}</span> },
              { key: 'period', header: '기간', render: (row) => <span className="text-xs">{row.periodStart} ~ {row.periodEnd}</span> },
              { key: 'status', header: '상태', render: (row) => <span className="text-xs">{SETTLEMENT_STATUS_LABEL[row.status] ?? row.status}</span> },
              { key: 'netAmount', header: '순정산', render: (row) => <span className="text-xs font-semibold">{formatPrice(row.netAmount)}</span> },
              { key: 'closedAt', header: '마감일', render: (row) => <span className="text-xs">{row.closedAt ? formatDateTime(row.closedAt) : '-'}</span> },
            ]}
          />
        </ReportSection>
      </div>
    </AdminLayout>
  );
}

function ReportSection({ title, description, children }: { title: string; description: string; children: ReactNode }) {
  return (
    <section>
      <div className="mb-3">
        <h2 className="text-sm font-semibold text-[#1a1f2e]">{title}</h2>
        <p className="mt-1 text-xs text-[#8a9bb5]">{description}</p>
      </div>
      {children}
    </section>
  );
}

function MiniMetric({ label, value }: { label: string; value: number }) {
  return (
    <div className="border border-[#e8eaf0] bg-white px-3 py-2">
      <div className="text-[11px] text-[#8a9bb5]">{label}</div>
      <div className="mt-1 text-sm font-semibold text-[#1a1f2e]">{value.toLocaleString()}건</div>
    </div>
  );
}

function StatusMessage({ message, tone }: { message: string; tone: 'error' | 'info' }) {
  const className = tone === 'error'
    ? 'border-red-100 bg-red-50 text-red-600'
    : 'border-blue-100 bg-blue-50 text-blue-600';
  return <div className={`mb-4 border px-4 py-3 text-sm ${className}`}>{message}</div>;
}
