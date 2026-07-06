'use client';

import { useState, useEffect } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import {
  returnService,
  type ApiReturn,
  type ApiReturnShipmentInfo,
  type ReturnShipmentPayload,
  type ReturnShipmentStatus,
  type ReturnShippingFeePayer,
} from '@/lib/services/returnService';
import {
  formatDateTime,
  formatPrice,
  RETURN_STATUS_LABEL,
  RETURN_STATUS_COLOR,
  RETURN_REASON_LABEL,
  RETURN_SHIPMENT_STATUS_LABEL,
  RETURN_SHIPPING_FEE_PAYER_LABEL,
} from '@/lib/format';

const PAGE_SIZE = 15;

type StatusFilter = 'ALL' | 'REQUESTED' | 'APPROVED' | 'REJECTED';

const STATUS_FILTERS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'REQUESTED', label: '반품 요청' },
  { value: 'APPROVED', label: '승인 완료' },
  { value: 'REJECTED', label: '거절' },
];

const RETURN_SHIPMENT_STATUS_OPTIONS: { value: ReturnShipmentStatus; label: string }[] = [
  { value: 'NOT_REQUESTED', label: '수거 미요청' },
  { value: 'COLLECTION_REQUESTED', label: '수거 요청' },
  { value: 'IN_TRANSIT', label: '회수 배송중' },
  { value: 'RECEIVED', label: '입고 완료' },
  { value: 'CANCELLED', label: '수거 취소' },
];

const FEE_PAYER_OPTIONS: { value: ReturnShippingFeePayer; label: string }[] = [
  { value: 'UNDECIDED', label: '미정' },
  { value: 'CUSTOMER', label: '고객 부담' },
  { value: 'COMPANY', label: '회사 부담' },
];

interface ShipmentFormState {
  carrier: string;
  trackingNumber: string;
  status: ReturnShipmentStatus;
  shippingFee: string;
  feePayer: ReturnShippingFeePayer;
  memo: string;
}

const emptyShipmentForm: ShipmentFormState = {
  carrier: '',
  trackingNumber: '',
  status: 'NOT_REQUESTED',
  shippingFee: '',
  feePayer: 'UNDECIDED',
  memo: '',
};

function toShipmentForm(info: ApiReturnShipmentInfo): ShipmentFormState {
  return {
    carrier: info.carrier ?? '',
    trackingNumber: info.trackingNumber ?? '',
    status: info.status ?? 'NOT_REQUESTED',
    shippingFee: info.shippingFee != null ? String(info.shippingFee) : '',
    feePayer: info.feePayer ?? 'UNDECIDED',
    memo: info.memo ?? '',
  };
}

function toShipmentPayload(form: ShipmentFormState): ReturnShipmentPayload {
  const fee = form.shippingFee.trim();
  return {
    carrier: form.carrier.trim() || undefined,
    trackingNumber: form.trackingNumber.trim() || undefined,
    status: form.status,
    shippingFee: fee ? Number(fee) : null,
    feePayer: form.feePayer,
    memo: form.memo.trim() || undefined,
  };
}

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
  const [selectedReturn, setSelectedReturn] = useState<ApiReturn | null>(null);
  const [shipmentInfo, setShipmentInfo] = useState<ApiReturnShipmentInfo | null>(null);
  const [shipmentForm, setShipmentForm] = useState<ShipmentFormState>(emptyShipmentForm);
  const [shipmentLoading, setShipmentLoading] = useState(false);
  const [shipmentError, setShipmentError] = useState('');
  const [shipmentSaving, setShipmentSaving] = useState(false);

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
    const note = prompt('관리자 메모를 입력하세요. 선택 사항입니다.') ?? undefined;
    try {
      await returnService.approveReturn(returnId, note);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '승인 처리에 실패했습니다.');
    }
  };

  const handleReject = async (returnId: number) => {
    const note = prompt('거절 사유를 입력하세요. 선택 사항입니다.') ?? undefined;
    try {
      await returnService.rejectReturn(returnId, note);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '거절 처리에 실패했습니다.');
    }
  };

  const openShipmentPanel = async (returnItem: ApiReturn) => {
    setSelectedReturn(returnItem);
    setShipmentInfo(null);
    setShipmentForm(emptyShipmentForm);
    setShipmentError('');
    setShipmentLoading(true);

    try {
      const info = await returnService.getReturnShipment(returnItem.returnId);
      setShipmentInfo(info);
      setShipmentForm(toShipmentForm(info));
    } catch (err) {
      setShipmentError(err instanceof Error ? err.message : '반품 배송 정보를 불러오지 못했습니다.');
    } finally {
      setShipmentLoading(false);
    }
  };

  const handleSaveShipment = async () => {
    if (!selectedReturn) return;
    if (shipmentForm.shippingFee.trim() && Number.isNaN(Number(shipmentForm.shippingFee))) {
      setShipmentError('배송비는 숫자로 입력해주세요.');
      return;
    }

    setShipmentSaving(true);
    setShipmentError('');
    try {
      const saved = await returnService.saveReturnShipment(
        selectedReturn.returnId,
        toShipmentPayload(shipmentForm),
        Boolean(shipmentInfo?.id)
      );
      setShipmentInfo(saved);
      setShipmentForm(toShipmentForm(saved));
    } catch (err) {
      setShipmentError(err instanceof Error ? err.message : '반품 배송 정보 저장에 실패했습니다.');
    } finally {
      setShipmentSaving(false);
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
          placeholder="주문번호 또는 고객명 검색"
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
              render: (row) => <span className="text-xs text-[#777]">{row.adminNote ?? '-'}</span>,
            },
            {
              key: 'createdAt',
              header: '요청일시',
              render: (row) => <span className="text-xs">{formatDateTime(row.createdAt)}</span>,
            },
            {
              key: 'actions',
              header: '관리',
              render: (row) => (
                <div className="flex flex-wrap gap-2">
                  <Button variant="outline" size="sm" onClick={() => openShipmentPanel(row)}>
                    배송 정보
                  </Button>
                  {row.status === 'REQUESTED' ? (
                    <>
                      <Button variant="primary" size="sm" onClick={() => handleApprove(row.returnId)}>
                        승인
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleReject(row.returnId)}>
                        거절
                      </Button>
                    </>
                  ) : (
                    <span className="text-xs text-[#aaa] self-center">처리 완료</span>
                  )}
                </div>
              ),
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

      {selectedReturn && (
        <section className="mt-6 border border-[#e8eaf0] bg-white p-5">
          <div className="flex items-start justify-between gap-4 mb-4">
            <div>
              <h2 className="text-base font-semibold text-[#1a1f2e]">반품 배송 정보</h2>
              <p className="text-xs text-[#8a9bb5] mt-1">
                주문번호 {selectedReturn.orderNumber} · {selectedReturn.userName}
              </p>
            </div>
            <button className="text-xs text-[#8a9bb5] hover:text-[#1a1f2e]" onClick={() => setSelectedReturn(null)}>
              닫기
            </button>
          </div>

          {shipmentLoading ? (
            <div className="py-8 text-center text-sm text-[#aaa]">배송 정보를 불러오는 중...</div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
              <div className="space-y-3">
                <label className="block text-xs font-medium text-[#666]">
                  택배사
                  <input
                    value={shipmentForm.carrier}
                    onChange={(e) => setShipmentForm((prev) => ({ ...prev, carrier: e.target.value }))}
                    className="mt-1 w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
                    placeholder="예: CJ대한통운"
                  />
                </label>
                <label className="block text-xs font-medium text-[#666]">
                  수거 송장번호
                  <input
                    value={shipmentForm.trackingNumber}
                    onChange={(e) => setShipmentForm((prev) => ({ ...prev, trackingNumber: e.target.value }))}
                    className="mt-1 w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
                    placeholder="수거 송장번호"
                  />
                </label>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <label className="block text-xs font-medium text-[#666]">
                    수거 상태
                    <select
                      value={shipmentForm.status}
                      onChange={(e) => setShipmentForm((prev) => ({ ...prev, status: e.target.value as ReturnShipmentStatus }))}
                      className="mt-1 w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white"
                    >
                      {RETURN_SHIPMENT_STATUS_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>{option.label}</option>
                      ))}
                    </select>
                  </label>
                  <label className="block text-xs font-medium text-[#666]">
                    배송비 부담
                    <select
                      value={shipmentForm.feePayer}
                      onChange={(e) => setShipmentForm((prev) => ({ ...prev, feePayer: e.target.value as ReturnShippingFeePayer }))}
                      className="mt-1 w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white"
                    >
                      {FEE_PAYER_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>{option.label}</option>
                      ))}
                    </select>
                  </label>
                </div>
                <label className="block text-xs font-medium text-[#666]">
                  반품 배송비
                  <input
                    value={shipmentForm.shippingFee}
                    onChange={(e) => setShipmentForm((prev) => ({ ...prev, shippingFee: e.target.value }))}
                    className="mt-1 w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
                    placeholder="0"
                    inputMode="numeric"
                  />
                </label>
                <label className="block text-xs font-medium text-[#666]">
                  배송 메모
                  <textarea
                    value={shipmentForm.memo}
                    onChange={(e) => setShipmentForm((prev) => ({ ...prev, memo: e.target.value }))}
                    className="mt-1 w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] min-h-[84px]"
                    placeholder="수거 요청, 입고 확인 등 운영 메모"
                  />
                </label>
                {shipmentError && <p className="text-xs text-[#c43a3a]">{shipmentError}</p>}
                <Button variant="primary" size="sm" onClick={handleSaveShipment} disabled={shipmentSaving}>
                  {shipmentSaving ? '저장 중...' : '배송 정보 저장'}
                </Button>
              </div>

              <div className="bg-[#f8f9fb] border border-[#eef0f4] p-4 text-sm text-[#555] space-y-3">
                <div className="flex justify-between gap-4">
                  <span className="text-[#8a9bb5]">현재 상태</span>
                  <span className="font-medium text-[#1a1f2e]">
                    {RETURN_SHIPMENT_STATUS_LABEL[shipmentInfo?.status ?? shipmentForm.status] ?? shipmentForm.status}
                  </span>
                </div>
                <div className="flex justify-between gap-4">
                  <span className="text-[#8a9bb5]">배송비 부담</span>
                  <span>{RETURN_SHIPPING_FEE_PAYER_LABEL[shipmentInfo?.feePayer ?? shipmentForm.feePayer] ?? '-'}</span>
                </div>
                <div className="flex justify-between gap-4">
                  <span className="text-[#8a9bb5]">배송비</span>
                  <span>{shipmentInfo?.shippingFee != null ? formatPrice(shipmentInfo.shippingFee) : '-'}</span>
                </div>
                <div className="flex justify-between gap-4">
                  <span className="text-[#8a9bb5]">저장 여부</span>
                  <span>{shipmentInfo?.id ? '저장됨' : '아직 저장되지 않음'}</span>
                </div>
                <p className="text-xs leading-5 text-[#8a9bb5] pt-2 border-t border-[#e8eaf0]">
                  실제 택배사 수거 API 연동과 자동 회수 추적은 후속 범위입니다. 이번 화면에서는 수동으로 수거 송장과 상태를 관리합니다.
                </p>
              </div>
            </div>
          )}
        </section>
      )}
    </AdminLayout>
  );
}
