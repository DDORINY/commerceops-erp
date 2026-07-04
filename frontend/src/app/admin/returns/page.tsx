'use client';

import { useState, useEffect } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { returnService, type ApiReturn } from '@/lib/services/returnService';
import {
  formatDateTime,
  RETURN_STATUS_LABEL,
  RETURN_STATUS_COLOR,
  RETURN_REASON_LABEL,
} from '@/lib/format';

type StatusFilter = 'ALL' | 'REQUESTED' | 'APPROVED' | 'REJECTED';

const STATUS_FILTERS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'REQUESTED', label: '반품 요청' },
  { value: 'APPROVED', label: '승인 완료' },
  { value: 'REJECTED', label: '거절' },
];

const PAGE_SIZE = 15;

export default function AdminReturnsPage() {
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [returns, setReturns] = useState<ApiReturn[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const loadReturns = async () => {
      setLoading(true);
      setError('');

      try {
        const res = await returnService.getAdminReturns(
          statusFilter,
          searchKeyword || undefined,
          page - 1,
          PAGE_SIZE
        );
        if (!mounted) return;
        setReturns(res.content);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements);
      } catch (err) {
        if (!mounted) return;
        setReturns([]);
        setTotalPages(1);
        setTotalElements(0);
        setError(err instanceof Error ? err.message : '반품 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadReturns();

    return () => {
      mounted = false;
    };
  }, [statusFilter, searchKeyword, page, reloadKey]);

  const handleApprove = async (returnId: number) => {
    const note = prompt('관리자 메모 (선택)') ?? undefined;
    try {
      await returnService.approveReturn(returnId, note);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '승인 처리에 실패했습니다.');
    }
  };

  const handleReject = async (returnId: number) => {
    const note = prompt('거절 사유를 입력하세요:') ?? undefined;
    try {
      await returnService.rejectReturn(returnId, note);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '거절 처리에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="반품 관리">
      <div className="flex flex-wrap gap-2 mb-4">
        {STATUS_FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => { setStatusFilter(f.value); setPage(1); }}
            className={[
              'px-4 py-1.5 text-xs font-medium border transition-colors',
              statusFilter === f.value
                ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white'
                : 'border-[#e8eaf0] text-[#8a9bb5] hover:border-[#1a1f2e] hover:text-[#1a1f2e] bg-white',
            ].join(' ')}
          >
            {f.label}
          </button>
        ))}
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') { setSearchKeyword(keyword); setPage(1); } }}
          placeholder="주문번호, 고객명 검색"
          className="flex-1 border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <Button variant="primary" size="sm" onClick={() => { setSearchKeyword(keyword); setPage(1); }}>
          검색
        </Button>
      </div>

      {loading ? (
        <div className="py-12 text-center text-[#bbb] text-sm">로딩 중...</div>
      ) : error ? (
        <div className="bg-white border border-[#f1c7c7] px-5 py-12 text-center">
          <p className="text-sm text-[#c43a3a]">{error}</p>
          <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
            다시 불러오기
          </Button>
        </div>
      ) : (
        <DataTable<ApiReturn>
          keyField="returnId"
          data={returns}
          emptyMessage="반품 데이터가 없습니다."
          columns={[
            { key: 'orderNumber', header: '주문번호' },
            { key: 'userName', header: '고객명' },
            {
              key: 'reason',
              header: '반품 사유',
              render: (row) => (
                <span className="text-xs">
                  {RETURN_REASON_LABEL[row.reason] ?? row.reason}
                  {row.reasonDetail && (
                    <span className="text-[#999] ml-1">({row.reasonDetail})</span>
                  )}
                </span>
              ),
            },
            {
              key: 'status',
              header: '상태',
              render: (row) => (
                <span className={`text-xs font-medium px-2 py-0.5 ${RETURN_STATUS_COLOR[row.status] ?? ''}`}>
                  {RETURN_STATUS_LABEL[row.status] ?? row.status}
                </span>
              ),
            },
            {
              key: 'adminNote',
              header: '관리자 메모',
              render: (row) => <span className="text-xs text-[#777]">{row.adminNote ?? '—'}</span>,
            },
            {
              key: 'createdAt',
              header: '요청일시',
              render: (row) => <span className="text-xs">{formatDateTime(row.createdAt)}</span>,
            },
            {
              key: 'returnId',
              header: '관리',
              render: (row) => {
                if (row.status !== 'REQUESTED') {
                  return <span className="text-xs text-[#aaa]">처리 완료</span>;
                }
                return (
                  <div className="flex gap-2">
                    <Button variant="primary" size="sm" onClick={() => handleApprove(row.returnId)}>
                      승인
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => handleReject(row.returnId)}>
                      거절
                    </Button>
                  </div>
                );
              },
            },
          ]}
        />
      )}

      {!loading && !error && (
        <div className="mt-2 text-xs text-[#aaa]">총 {totalElements}건</div>
      )}
      {!loading && !error && (
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      )}
    </AdminLayout>
  );
}
