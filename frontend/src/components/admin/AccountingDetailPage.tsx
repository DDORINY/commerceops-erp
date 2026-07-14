'use client';

import { useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import {
  accountingService,
  type ApiAccountingTransaction,
  type ApiAdminPayment,
  type ApiPaymentStatus,
  type ApiSettlementBatch,
  type ApiSettlementBatchStatus,
} from '@/lib/services/accountingService';
import { formatDateTime, formatPrice } from '@/lib/format';

type View = 'payments' | 'refunds' | 'settlements';
type Row = ApiAdminPayment | ApiAccountingTransaction | ApiSettlementBatch;
const PAGE_SIZE = 20;

const PAYMENT_STATUS_LABEL: Record<ApiPaymentStatus, string> = {
  READY: '결제 대기', PAID: '결제 완료', FAILED: '결제 실패', CANCELLED: '취소', REFUNDED: '환불 완료',
};
const SETTLEMENT_STATUS_LABEL: Record<ApiSettlementBatchStatus, string> = {
  DRAFT: '작성 중', CONFIRMED: '확정', CLOSED: '마감', CANCELLED: '취소',
};

export default function AccountingDetailPage({ view }: { view: View }) {
  const [rows, setRows] = useState<Row[]>([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [status, setStatus] = useState('ALL');
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;
    const request = view === 'payments'
      ? accountingService.getPayments({ status: status as ApiPaymentStatus | 'ALL', keyword: searchKeyword, page: page - 1, size: PAGE_SIZE })
      : view === 'refunds'
        ? accountingService.getRefundEvents(page - 1, PAGE_SIZE)
        : accountingService.getSettlementBatches({ status: status as ApiSettlementBatchStatus | 'ALL', page: page - 1, size: PAGE_SIZE });
    request.then((res) => {
      if (!mounted) return;
      setRows(res.content);
      setTotalPages(res.totalPages || 1);
      setTotalElements(res.totalElements);
    }).catch((err) => {
      if (mounted) setError(err instanceof Error ? err.message : '내역을 불러오지 못했습니다.');
    }).finally(() => { if (mounted) setLoading(false); });
    return () => { mounted = false; };
  }, [view, status, searchKeyword, page]);

  const title = view === 'payments' ? '결제 내역' : view === 'refunds' ? '환불 내역' : '정산 관리';
  const statuses = view === 'payments'
    ? [['ALL', '전체'], ['READY', '결제 대기'], ['PAID', '결제 완료'], ['FAILED', '실패'], ['CANCELLED', '취소'], ['REFUNDED', '환불']]
    : view === 'settlements'
      ? [['ALL', '전체'], ['DRAFT', '작성 중'], ['CONFIRMED', '확정'], ['CLOSED', '마감'], ['CANCELLED', '취소']]
      : [];

  return (
    <AdminLayout title={title}>
      <section className="mb-5 border border-[#e8eaf0] bg-white p-5">
        <h2 className="text-base font-semibold text-[#1a1f2e]">{title}</h2>
        <p className="mt-1 text-sm text-[#6f7a8a]">
          {view === 'payments' ? '주문별 결제 수단, 승인 상태와 거래번호를 확인합니다.' : view === 'refunds' ? '회계에 반영된 결제·반품 환불 거래를 확인합니다.' : '기간별 정산 금액과 마감 상태를 확인합니다.'}
        </p>
      </section>

      {statuses.length > 0 && <div className="mb-4 flex flex-wrap gap-2">{statuses.map(([value, label]) => (
        <button key={value} type="button" onClick={() => { setLoading(true); setError(''); setStatus(value); setPage(1); }} className={`border px-4 py-1.5 text-xs font-medium ${status === value ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white' : 'border-[#e8eaf0] bg-white text-[#8a9bb5]'}`}>{label}</button>
      ))}</div>}

      {view === 'payments' && <div className="mb-4 flex gap-3 border border-[#e8eaf0] bg-white p-4">
        <input value={keyword} onChange={(e) => setKeyword(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter') { setLoading(true); setError(''); setSearchKeyword(keyword.trim()); setPage(1); } }} placeholder="주문번호 또는 고객명 검색" className="min-w-0 flex-1 border border-[#dfe3ea] px-3 py-2 text-sm" />
        <Button size="sm" onClick={() => { setLoading(true); setError(''); setSearchKeyword(keyword.trim()); setPage(1); }}>검색</Button>
      </div>}

      {error && <div className="mb-4 border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">{error}</div>}
      {loading ? <div className="py-12 text-center text-sm text-[#bbb]">로딩 중...</div> : <DetailTable view={view} rows={rows} />}
      <div className="mt-3 text-xs text-[#aaa]">총 {totalElements.toLocaleString()}건</div>
      <Pagination currentPage={page} totalPages={totalPages} onPageChange={(nextPage) => { setLoading(true); setError(''); setPage(nextPage); }} />
    </AdminLayout>
  );
}

function DetailTable({ view, rows }: { view: View; rows: Row[] }) {
  if (view === 'payments') return <DataTable<ApiAdminPayment> keyField="paymentId" data={rows as ApiAdminPayment[]} emptyMessage="결제 내역이 없습니다." columns={[
    { key: 'orderNumber', header: '주문번호' },
    { key: 'userName', header: '고객명' },
    { key: 'paymentMethod', header: '결제 수단' },
    { key: 'paymentStatus', header: '상태', render: (row) => PAYMENT_STATUS_LABEL[row.paymentStatus] ?? row.paymentStatus },
    { key: 'paidAmount', header: '결제 금액', render: (row) => formatPrice(row.paidAmount ?? 0) },
    { key: 'transactionId', header: '거래번호', render: (row) => <span className="font-mono text-xs">{row.transactionId ?? '-'}</span> },
    { key: 'createdAt', header: '결제 일시', render: (row) => formatDateTime(row.createdAt) },
  ]} />;
  if (view === 'refunds') return <DataTable<ApiAccountingTransaction> keyField="transactionId" data={rows as ApiAccountingTransaction[]} emptyMessage="환불 내역이 없습니다." columns={[
    { key: 'transactionNumber', header: '환불 거래번호' },
    { key: 'referenceType', header: '원천' },
    { key: 'referenceId', header: '참조 ID', render: (row) => `#${row.referenceId}` },
    { key: 'amount', header: '환불 금액', render: (row) => <span className="font-semibold text-red-500">-{formatPrice(row.amount)}</span> },
    { key: 'memo', header: '내용', render: (row) => row.memo ?? '-' },
    { key: 'occurredAt', header: '환불 일시', render: (row) => formatDateTime(row.occurredAt) },
  ]} />;
  return <DataTable<ApiSettlementBatch> keyField="id" data={rows as ApiSettlementBatch[]} emptyMessage="정산 배치가 없습니다." columns={[
    { key: 'batchNumber', header: '배치 번호' },
    { key: 'period', header: '정산 기간', render: (row) => `${row.periodStart} ~ ${row.periodEnd}` },
    { key: 'status', header: '상태', render: (row) => SETTLEMENT_STATUS_LABEL[row.status] ?? row.status },
    { key: 'totalSales', header: '매출', render: (row) => formatPrice(row.totalSales) },
    { key: 'totalRefunds', header: '환불', render: (row) => formatPrice(row.totalRefunds) },
    { key: 'netAmount', header: '순정산', render: (row) => <span className="font-semibold">{formatPrice(row.netAmount)}</span> },
    { key: 'closedAt', header: '마감일', render: (row) => row.closedAt ? formatDateTime(row.closedAt) : '-' },
  ]} />;
}
