'use client';

import { useState, useEffect } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { shipmentService, type ApiShipment } from '@/lib/services/shipmentService';
import {
  formatDateTime,
  SHIPMENT_STATUS_LABEL,
  SHIPMENT_STATUS_COLOR,
  CARRIER_OPTIONS,
} from '@/lib/format';

type StatusFilter = 'ALL' | 'READY' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';

const STATUS_FILTERS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'READY', label: '배송준비' },
  { value: 'IN_TRANSIT', label: '배송중' },
  { value: 'DELIVERED', label: '배송완료' },
  { value: 'CANCELLED', label: '배송취소' },
];

const PAGE_SIZE = 15;

export default function AdminShipmentsPage() {
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [shipments, setShipments] = useState<ApiShipment[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  // inline tracking form state: shipmentId → { trackingNumber, carrier }
  const [editingId, setEditingId] = useState<number | null>(null);
  const [trackingInput, setTrackingInput] = useState('');
  const [carrierInput, setCarrierInput] = useState(CARRIER_OPTIONS[0]);

  useEffect(() => {
    let mounted = true;

    const loadShipments = async () => {
      setLoading(true);
      setError('');

      try {
        const res = await shipmentService.getAdminShipments(
          statusFilter,
          searchKeyword || undefined,
          page - 1,
          PAGE_SIZE
        );
        if (!mounted) return;
        setShipments(res.content);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements);
      } catch (err) {
        if (!mounted) return;
        setShipments([]);
        setTotalPages(1);
        setTotalElements(0);
        setError(err instanceof Error ? err.message : '배송 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadShipments();

    return () => {
      mounted = false;
    };
  }, [statusFilter, searchKeyword, page, reloadKey]);

  const handleTrackingSubmit = async (shipmentId: number) => {
    if (!trackingInput.trim()) {
      alert('송장번호를 입력하세요.');
      return;
    }
    try {
      await shipmentService.updateTracking(shipmentId, trackingInput.trim(), carrierInput);
      setEditingId(null);
      setTrackingInput('');
      setCarrierInput(CARRIER_OPTIONS[0]);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '송장 등록에 실패했습니다.');
    }
  };

  const handleGenerateTrackingNumber = async (shipmentId: number) => {
    try {
      await shipmentService.generateTrackingNumber(shipmentId, carrierInput);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '송장번호 자동 생성에 실패했습니다.');
    }
  };

  const handleDeliver = async (shipmentId: number) => {
    if (!confirm('배송완료로 처리하시겠습니까?')) return;
    try {
      await shipmentService.markDelivered(shipmentId);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '배송완료 처리에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="배송 관리">
      {/* 상태 필터 */}
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

      {/* 검색 */}
      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') { setSearchKeyword(keyword); setPage(1); } }}
          placeholder="주문번호, 수령인 검색"
          className="flex-1 border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <Button variant="primary" size="sm" onClick={() => { setSearchKeyword(keyword); setPage(1); }}>
          검색
        </Button>
      </div>

      {/* 테이블 */}
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
        <DataTable<ApiShipment>
          keyField="shipmentId"
          data={shipments}
          emptyMessage="배송 데이터가 없습니다."
          columns={[
            { key: 'orderNumber', header: '주문번호' },
            { key: 'receiverName', header: '수령인' },
            {
              key: 'address',
              header: '배송지',
              render: (row) => (
                <span className="text-xs text-[#666] max-w-[200px] block truncate" title={row.address}>
                  {row.address}
                </span>
              ),
            },
            {
              key: 'status',
              header: '상태',
              render: (row) => (
                <span className={`text-xs font-medium px-2 py-0.5 ${SHIPMENT_STATUS_COLOR[row.status] ?? ''}`}>
                  {SHIPMENT_STATUS_LABEL[row.status] ?? row.status}
                </span>
              ),
            },
            {
              key: 'carrier',
              header: '택배사',
              render: (row) => <span className="text-xs">{row.carrier ?? '—'}</span>,
            },
            {
              key: 'trackingNumber',
              header: '송장번호',
              render: (row) => <span className="text-xs font-mono">{row.trackingNumber ?? '—'}</span>,
            },
            {
              key: 'trackingNumberSource',
              header: '발급 방식',
              render: (row) => (
                <span className="text-xs">
                  {row.trackingNumberSource === 'SYSTEM' ? '자동 생성' : row.trackingNumberSource === 'MANUAL' ? '수동 입력' : '—'}
                </span>
              ),
            },
            {
              key: 'shippedAt',
              header: '배송시작',
              render: (row) => (
                <span className="text-xs">{row.shippedAt ? formatDateTime(row.shippedAt) : '—'}</span>
              ),
            },
            {
              key: 'shipmentId',
              header: '관리',
              render: (row) => {
                if (row.status === 'DELIVERED' || row.status === 'CANCELLED') {
                  return (
                    <span className="text-xs text-[#aaa]">
                      {row.status === 'CANCELLED' ? '취소됨' : row.deliveredAt ? formatDateTime(row.deliveredAt) : '완료'}
                    </span>
                  );
                }

                if (editingId === row.shipmentId) {
                  return (
                    <div className="flex flex-col gap-1.5 min-w-[240px]">
                      <select
                        value={carrierInput}
                        onChange={(e) => setCarrierInput(e.target.value)}
                        className="border border-[#e0e0e0] px-2 py-1 text-xs outline-none bg-white"
                      >
                        {CARRIER_OPTIONS.map((c) => (
                          <option key={c} value={c}>{c}</option>
                        ))}
                      </select>
                      <div className="flex gap-1">
                        <input
                          autoFocus
                          type="text"
                          value={trackingInput}
                          onChange={(e) => setTrackingInput(e.target.value)}
                          onKeyDown={(e) => { if (e.key === 'Enter') handleTrackingSubmit(row.shipmentId); }}
                          placeholder="송장번호 입력"
                          className="flex-1 border border-[#e0e0e0] px-2 py-1 text-xs outline-none focus:border-[#1a1f2e] font-mono"
                        />
                        <Button variant="primary" size="sm" onClick={() => handleTrackingSubmit(row.shipmentId)}>
                          저장
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setEditingId(null);
                            setTrackingInput('');
                            setCarrierInput(CARRIER_OPTIONS[0]);
                          }}
                        >
                          취소
                        </Button>
                      </div>
                    </div>
                  );
                }

                if (row.status === 'READY') {
                  return (
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setEditingId(row.shipmentId);
                          setTrackingInput('');
                          setCarrierInput(CARRIER_OPTIONS[0]);
                        }}
                      >
                        송장 입력
                      </Button>
                      <Button variant="ghost" size="sm" onClick={() => handleGenerateTrackingNumber(row.shipmentId)}>
                        자동 생성
                      </Button>
                    </div>
                  );
                }

                // IN_TRANSIT
                return (
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => {
                        setEditingId(row.shipmentId);
                        setTrackingInput(row.trackingNumber ?? '');
                        setCarrierInput(row.carrier ?? CARRIER_OPTIONS[0]);
                      }}
                    >
                      송장 수정
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => handleDeliver(row.shipmentId)}>
                      배송완료
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
