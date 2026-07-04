'use client';

import { useState, useEffect } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { orderService, type ApiAdminOrder } from '@/lib/services/orderService';
import { formatPrice, formatDateTime, ORDER_STATUS_LABEL, ORDER_STATUS_COLOR, downloadCsv } from '@/lib/format';

type StatusFilter = string;

const STATUS_FILTERS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: '전체' },
  { value: 'PENDING', label: '결제 대기' },
  { value: 'PAID', label: '결제 완료' },
  { value: 'PREPARING', label: '상품 준비중' },
  { value: 'SHIPPING', label: '배송중' },
  { value: 'COMPLETED', label: '배송 완료' },
  { value: 'CANCELLED', label: '주문 취소' },
];

const PAGE_SIZE = 10;

const PAYMENT_STATUS_LABEL: Record<string, string> = {
  READY: '결제 대기',
  PAID: '결제 완료',
  FAILED: '결제 실패',
  CANCELLED: '결제 취소',
  REFUNDED: '환불 완료',
};

const PAYMENT_STATUS_COLOR: Record<string, string> = {
  READY: 'bg-gray-100 text-gray-600',
  PAID: 'bg-green-50 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
  CANCELLED: 'bg-gray-100 text-gray-500',
  REFUNDED: 'bg-amber-50 text-amber-700',
};

const NEXT_STATUSES: Record<string, { value: string; label: string }[]> = {
  PENDING: [{ value: 'CANCELLED', label: '주문 취소' }],
  PAID: [
    { value: 'PREPARING', label: '상품 준비' },
    { value: 'CANCELLED', label: '취소/환불' },
  ],
  PREPARING: [
    { value: 'SHIPPING', label: '배송 시작' },
    { value: 'CANCELLED', label: '취소/환불' },
  ],
  SHIPPING: [{ value: 'COMPLETED', label: '배송 완료' }],
};

export default function AdminOrdersPage() {
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [orders, setOrders] = useState<ApiAdminOrder[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const loadOrders = async () => {
      setLoading(true);
      setError('');

      try {
        const res = await orderService.getAdminOrders(
          statusFilter,
          searchKeyword || undefined,
          page - 1,
          PAGE_SIZE
        );
        if (!mounted) return;
        setOrders(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages || 1);
      } catch (err) {
        if (!mounted) return;
        setOrders([]);
        setTotalElements(0);
        setTotalPages(1);
        setError(err instanceof Error ? err.message : '주문 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadOrders();

    return () => {
      mounted = false;
    };
  }, [statusFilter, searchKeyword, page, reloadKey]);

  const handleStatusChange = async (orderId: number, newStatus: string) => {
    if (!newStatus) return;
    if (newStatus === 'CANCELLED' && !confirm('결제 및 재고 복구를 포함해 주문을 취소하시겠습니까?')) return;
    try {
      await orderService.updateOrderStatus(orderId, newStatus);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '상태 변경에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="주문 관리">
      <div className="flex items-center justify-between mb-5">
        <p className="text-sm text-[#8a9bb5]">
          총 <span className="font-semibold text-[#1a1f2e]">{totalElements}</span>건
        </p>
      </div>

      <div className="flex flex-wrap gap-2 mb-4">
        {STATUS_FILTERS.map((filter) => (
          <button
            key={filter.value}
            onClick={() => { setStatusFilter(filter.value); setPage(1); }}
            className={[
              'px-4 py-1.5 text-xs font-medium border transition-colors',
              statusFilter === filter.value
                ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white'
                : 'border-[#e8eaf0] text-[#8a9bb5] hover:border-[#1a1f2e] hover:text-[#1a1f2e] bg-white',
            ].join(' ')}
          >
            {filter.label}
          </button>
        ))}
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') { setSearchKeyword(keyword); setPage(1); }
          }}
          placeholder="주문번호, 고객명 검색"
          className="flex-1 border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <Button variant="primary" size="sm" onClick={() => { setSearchKeyword(keyword); setPage(1); }}>
          검색
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            downloadCsv(
              `orders_${new Date().toISOString().slice(0,10)}.csv`,
              ['주문ID', '주문번호', '고객명', '고객 이메일', '상품 개수', '결제금액', '주문 상태', '결제 상태', '주문일'],
              orders.map((o) => [
                o.orderId,
                o.orderNumber,
                o.userName,
                o.userEmail,
                o.itemCount,
                o.totalPrice,
                ORDER_STATUS_LABEL[o.status] ?? o.status,
                PAYMENT_STATUS_LABEL[o.paymentStatus] ?? o.paymentStatus,
                formatDateTime(o.createdAt),
              ])
            );
          }}
        >
          CSV 다운로드
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
        <DataTable<ApiAdminOrder>
          keyField="orderId"
          data={orders}
          emptyMessage="주문 데이터가 없습니다."
          columns={[
            { key: 'orderId', header: 'ID' },
            { key: 'orderNumber', header: '주문번호' },
            {
              key: 'userName',
              header: '주문자',
              render: (row) => (
                <div>
                  <p className="font-medium text-[#222]">{row.userName}</p>
                  <p className="text-xs text-[#999]">{row.userEmail}</p>
                </div>
              ),
            },
            { key: 'receiverName', header: '수령인' },
            {
              key: 'itemCount',
              header: '상품 수',
              render: (row) => `${row.itemCount}개`,
            },
            {
              key: 'totalPrice',
              header: '결제금액',
              render: (row) => formatPrice(row.totalPrice),
            },
            {
              key: 'status',
              header: '주문 상태',
              render: (row) => (
                <span className={`text-xs font-medium px-2 py-0.5 ${ORDER_STATUS_COLOR[row.status] ?? ''}`}>
                  {ORDER_STATUS_LABEL[row.status] ?? row.status}
                </span>
              ),
            },
            {
              key: 'paymentStatus',
              header: '결제 상태',
              render: (row) => (
                <span className={`text-xs font-medium px-2 py-0.5 ${PAYMENT_STATUS_COLOR[row.paymentStatus] ?? ''}`}>
                  {PAYMENT_STATUS_LABEL[row.paymentStatus] ?? row.paymentStatus}
                </span>
              ),
            },
            {
              key: 'createdAt',
              header: '주문일시',
              render: (row) => formatDateTime(row.createdAt),
            },
            {
              key: 'orderId',
              header: '관리',
              render: (row) => (
                <div className="flex gap-2">
                  {(NEXT_STATUSES[row.status] ?? []).length > 0 ? (
                    <select
                      value=""
                      className="border border-[#e0e0e0] px-2 py-1 text-xs outline-none bg-white"
                      onClick={(e) => e.stopPropagation()}
                      onChange={(e) => handleStatusChange(row.orderId, e.target.value)}
                    >
                      <option value="">상태 변경</option>
                      {(NEXT_STATUSES[row.status] ?? []).map((option) => (
                        <option key={option.value} value={option.value}>{option.label}</option>
                      ))}
                    </select>
                  ) : <span className="text-xs text-[#aaa]">처리 완료</span>}
                </div>
              ),
            },
          ]}
        />
      )}

      {!loading && !error && (
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      )}
    </AdminLayout>
  );
}
