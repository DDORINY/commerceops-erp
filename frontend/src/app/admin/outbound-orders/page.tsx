'use client';

import { FormEvent, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { getUserRole } from '@/lib/auth';
import { permissionGroupService } from '@/lib/services/permissionGroupService';
import {
  outboundOrderService,
  type ApiOutboundOrder,
  type ApiOutboundOrderItem,
  type OutboundOrderSaveRequest,
  type OutboundOrderStatus,
} from '@/lib/services/outboundOrderService';

const PAGE_SIZE = 10;

const STATUS_LABEL: Record<OutboundOrderStatus, string> = {
  REQUESTED: '출고 요청',
  PICKING: '피킹 중',
  PICKED: '피킹 완료',
  SHIPPED: '배송 완료',
  CANCELLED: '취소',
};

type StatusFilter = OutboundOrderStatus | 'ALL';

type FormState = {
  orderId: string;
  warehouseId: string;
  memo: string;
};

const emptyForm: FormState = {
  orderId: '',
  warehouseId: '',
  memo: '',
};

export default function AdminOutboundOrdersPage() {
  const [items, setItems] = useState<ApiOutboundOrder[]>([]);
  const [selected, setSelected] = useState<ApiOutboundOrder | null>(null);
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [warehouseIdFilter, setWarehouseIdFilter] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<ApiOutboundOrder | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);

  const role = getUserRole();
  const hasOutboundManage =
    role === 'SUPER_ADMIN' ||
    permissionCodes.includes('OUTBOUND_MANAGE') ||
    (!permissionCodes.length && role === 'ADMIN');

  useEffect(() => {
    permissionGroupService.getMyEffectivePermissions()
      .then((response) => setPermissionCodes(response.permissionCodes))
      .catch(() => setPermissionCodes([]));
  }, []);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await outboundOrderService.getOutboundOrders({
          status: statusFilter,
          warehouseId: warehouseIdFilter ? Number(warehouseIdFilter) : undefined,
          keyword: searchKeyword || undefined,
          page: page - 1,
          size: PAGE_SIZE,
        });
        if (!mounted) return;
        setItems(response.content);
        setTotalPages(response.totalPages || 1);
        setTotalElements(response.totalElements);
      } catch (err) {
        if (!mounted) return;
        setItems([]);
        setTotalPages(1);
        setTotalElements(0);
        setError(err instanceof Error ? err.message : '출고 지시 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    load();
    return () => {
      mounted = false;
    };
  }, [statusFilter, warehouseIdFilter, searchKeyword, page, reloadKey]);

  const openCreateForm = () => {
    setEditing(null);
    setForm(emptyForm);
    setError('');
    setMessage('');
    setFormOpen(true);
  };

  const openEditForm = (row: ApiOutboundOrder) => {
    setEditing(row);
    setForm({
      orderId: String(row.orderId),
      warehouseId: String(row.warehouseId),
      memo: row.memo ?? '',
    });
    setError('');
    setMessage('');
    setFormOpen(true);
  };

  const closeForm = () => {
    if (saving) return;
    setFormOpen(false);
    setEditing(null);
    setForm(emptyForm);
  };

  const handleSearch = () => {
    setSearchKeyword(keyword);
    setPage(1);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!hasOutboundManage) {
      setError('출고 지시를 저장할 권한이 없습니다.');
      return;
    }

    const warehouseId = Number(form.warehouseId);
    if (!warehouseId || Number.isNaN(warehouseId)) {
      setError('창고 ID를 입력해주세요.');
      return;
    }

    const payload: OutboundOrderSaveRequest = {
      warehouseId,
      memo: normalize(form.memo),
    };

    if (!editing) {
      const orderId = Number(form.orderId);
      if (!orderId || Number.isNaN(orderId)) {
        setError('주문 ID를 입력해주세요.');
        return;
      }
      payload.orderId = orderId;
    }

    setSaving(true);
    setError('');
    setMessage('');
    try {
      if (editing) {
        await outboundOrderService.updateOutboundOrder(editing.id, payload);
        setMessage('출고 지시가 수정되었습니다.');
      } else {
        await outboundOrderService.createOutboundOrder(payload);
        setMessage('출고 지시가 생성되었습니다.');
      }
      closeForm();
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '출고 지시 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handlePick = async (row: ApiOutboundOrder) => {
    if (!hasOutboundManage) {
      setError('출고 지시를 피킹 처리할 권한이 없습니다.');
      return;
    }
    try {
      await outboundOrderService.pickOutboundOrder(row.id);
      setMessage('출고 지시가 피킹 완료 처리되었습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '피킹 처리에 실패했습니다.');
    }
  };

  const handleCancel = async (row: ApiOutboundOrder) => {
    if (!hasOutboundManage) {
      setError('출고 지시를 취소할 권한이 없습니다.');
      return;
    }
    if (!confirm(`${row.outboundNumber} 출고 지시를 취소할까요?`)) return;
    try {
      await outboundOrderService.cancelOutboundOrder(row.id);
      setMessage('출고 지시가 취소되었습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '출고 지시 취소에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="출고 관리">
      <div className="flex items-center justify-between gap-3 mb-5">
        <div>
          <h1 className="text-xl font-semibold text-[#1a1f2e]">출고 관리</h1>
          <p className="mt-1 text-sm text-[#6f7a8a]">
            주문 기준으로 출고 지시를 생성하고 피킹 준비 상태를 관리합니다.
          </p>
        </div>
        <Button variant="primary" size="sm" onClick={openCreateForm} disabled={!hasOutboundManage}>
          출고 지시 생성
        </Button>
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex flex-wrap gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') handleSearch();
          }}
          placeholder="출고번호, 주문번호, 수령자, 상품명 검색"
          className="flex-1 min-w-[240px] border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <input
          type="number"
          value={warehouseIdFilter}
          onChange={(event) => {
            setWarehouseIdFilter(event.target.value);
            setPage(1);
          }}
          placeholder="창고 ID"
          className="w-[120px] border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <select
          value={statusFilter}
          onChange={(event) => {
            setStatusFilter(event.target.value as StatusFilter);
            setPage(1);
          }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none bg-white"
        >
          <option value="ALL">전체 상태</option>
          {Object.entries(STATUS_LABEL).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
        <Button variant="outline" size="sm" onClick={handleSearch}>
          검색
        </Button>
      </div>

      {message && (
        <div className="mb-3 border border-[#b7dfc1] bg-[#f0fff4] px-4 py-3 text-sm text-[#246b38]">{message}</div>
      )}
      {error && (
        <div className="mb-3 border border-[#f1c7c7] bg-[#fff6f6] px-4 py-3 text-sm text-[#c43a3a]">{error}</div>
      )}

      {loading ? (
        <div className="py-12 text-center text-[#999] text-sm bg-white border border-[#e8eaf0]">
          출고 지시 목록을 불러오는 중입니다.
        </div>
      ) : (
        <>
          <DataTable<ApiOutboundOrder>
            keyField="id"
            data={items}
            emptyMessage="등록된 출고 지시가 없습니다."
            columns={[
              { key: 'outboundNumber', header: '출고번호' },
              { key: 'orderNumber', header: '주문번호' },
              { key: 'customerName', header: '주문자' },
              { key: 'warehouseName', header: '창고' },
              { key: 'status', header: '상태', render: (row) => STATUS_LABEL[row.status] },
              { key: 'totalQuantity', header: '총 수량', render: (row) => `${row.totalQuantity}개` },
              { key: 'pickedQuantity', header: '피킹 수량', render: (row) => `${row.pickedQuantity}개` },
              { key: 'requestedAt', header: '요청일', render: (row) => formatDate(row.requestedAt) },
              {
                key: 'actions',
                header: '관리',
                render: (row) => (
                  <div className="flex flex-wrap gap-2">
                    <Button variant="outline" size="sm" onClick={() => setSelected(row)}>
                      상세
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => openEditForm(row)} disabled={!hasOutboundManage || row.status === 'SHIPPED' || row.status === 'CANCELLED'}>
                      수정
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handlePick(row)} disabled={!hasOutboundManage || row.status === 'PICKED' || row.status === 'SHIPPED' || row.status === 'CANCELLED'}>
                      피킹 완료
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handleCancel(row)} disabled={!hasOutboundManage || row.status === 'SHIPPED' || row.status === 'CANCELLED'}>
                      취소
                    </Button>
                  </div>
                ),
              },
            ]}
          />
          <div className="mt-2 text-xs text-[#8a9bb5]">총 {totalElements}건</div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      {selected && (
        <div className="mt-5 bg-white border border-[#e8eaf0] p-5">
          <div className="flex items-start justify-between gap-3 mb-4">
            <div>
              <h2 className="text-base font-semibold text-[#1a1f2e]">{selected.outboundNumber}</h2>
              <p className="mt-1 text-sm text-[#6f7a8a]">
                {selected.orderNumber} / {selected.customerName} / {selected.warehouseName}
              </p>
            </div>
            <Button variant="ghost" size="sm" onClick={() => setSelected(null)}>닫기</Button>
          </div>
          <DataTable<ApiOutboundOrderItem>
            keyField="id"
            data={selected.items}
            emptyMessage="출고 품목이 없습니다."
            columns={[
              { key: 'productName', header: '상품' },
              { key: 'skuCode', header: 'SKU', render: (row) => row.skuCode || '-' },
              { key: 'barcode', header: '바코드', render: (row) => row.barcode || '-' },
              { key: 'quantity', header: '지시 수량', render: (row) => `${row.quantity}개` },
              { key: 'pickedQuantity', header: '피킹 수량', render: (row) => `${row.pickedQuantity}개` },
              { key: 'scannedQuantity', header: '스캔 수량', render: (row) => `${row.scannedQuantity}개` },
            ]}
          />
        </div>
      )}

      {formOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4">
          <div className="w-full max-w-[560px] bg-white border border-[#dfe3ea] shadow-xl">
            <div className="flex items-center justify-between border-b border-[#edf0f5] px-5 py-4">
              <h2 className="text-base font-semibold text-[#1a1f2e]">{editing ? '출고 지시 수정' : '출고 지시 생성'}</h2>
              <button type="button" onClick={closeForm} className="text-sm text-[#6f7a8a] hover:text-[#1a1f2e]">
                닫기
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-5 space-y-4">
              <label className="block text-sm">
                <span className="block mb-1 text-[#566171]">주문 ID</span>
                <input
                  type="number"
                  value={form.orderId}
                  onChange={(event) => setForm((prev) => ({ ...prev, orderId: event.target.value }))}
                  disabled={Boolean(editing)}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e] disabled:bg-[#f7f8fa]"
                  placeholder="출고 지시를 만들 주문 ID"
                />
              </label>
              <label className="block text-sm">
                <span className="block mb-1 text-[#566171]">출고 창고 ID</span>
                <input
                  type="number"
                  value={form.warehouseId}
                  onChange={(event) => setForm((prev) => ({ ...prev, warehouseId: event.target.value }))}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]"
                  placeholder="출고할 창고 ID"
                />
              </label>
              <label className="block text-sm">
                <span className="block mb-1 text-[#566171]">메모</span>
                <textarea
                  value={form.memo}
                  onChange={(event) => setForm((prev) => ({ ...prev, memo: event.target.value }))}
                  rows={3}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]"
                  placeholder="출고 작업 메모"
                />
              </label>
              <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="ghost" size="sm" onClick={closeForm} disabled={saving}>
                  취소
                </Button>
                <Button type="submit" variant="primary" size="sm" disabled={saving || !hasOutboundManage}>
                  {saving ? '저장 중' : '저장'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AdminLayout>
  );
}

function normalize(value: string) {
  const trimmed = value.trim();
  return trimmed ? trimmed : undefined;
}

function formatDate(value: string | null) {
  if (!value) return '-';
  return new Date(value).toLocaleString('ko-KR');
}
