'use client';

import { FormEvent, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import { getUserRole } from '@/lib/auth';
import { permissionGroupService } from '@/lib/services/permissionGroupService';
import { productionService, type ApiProductionOrder, type ApiProductionReceipt, type ProductionOrderStatus } from '@/lib/services/productionService';
import { warehouseService, type ApiWarehouse } from '@/lib/services/warehouseService';

const PAGE_SIZE = 10;

const STATUS_LABEL: Record<ProductionOrderStatus, string> = {
  PLANNED: '계획',
  IN_PROGRESS: '진행 중',
  COMPLETED: '완료',
  CANCELLED: '취소',
};

type ItemForm = {
  skuId: string;
  plannedQuantity: string;
};

type OrderForm = {
  warehouseId: string;
  memo: string;
  items: ItemForm[];
};

const emptyForm: OrderForm = {
  warehouseId: '',
  memo: '',
  items: [{ skuId: '', plannedQuantity: '1' }],
};

export default function AdminProductionPage() {
  const [orders, setOrders] = useState<ApiProductionOrder[]>([]);
  const [receipts, setReceipts] = useState<ApiProductionReceipt[]>([]);
  const [warehouses, setWarehouses] = useState<ApiWarehouse[]>([]);
  const [statusFilter, setStatusFilter] = useState<ProductionOrderStatus | 'ALL'>('ALL');
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [editingOrder, setEditingOrder] = useState<ApiProductionOrder | null>(null);
  const [form, setForm] = useState<OrderForm>(emptyForm);
  const [selectedOrder, setSelectedOrder] = useState<ApiProductionOrder | null>(null);
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);

  const role = getUserRole();
  const canManageProduction = role === 'SUPER_ADMIN' || permissionCodes.includes('PRODUCTION_MANAGE') || (!permissionCodes.length && role === 'ADMIN');

  useEffect(() => {
    permissionGroupService.getMyEffectivePermissions()
      .then((response) => setPermissionCodes(response.permissionCodes))
      .catch(() => setPermissionCodes([]));
    warehouseService.getWarehouses()
      .then(setWarehouses)
      .catch(() => setWarehouses([]));
  }, []);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [orderPage, receiptPage] = await Promise.all([
          productionService.getOrders({
            status: statusFilter,
            keyword: searchKeyword || undefined,
            page: page - 1,
            size: PAGE_SIZE,
          }),
          productionService.getReceipts({ page: 0, size: 5 }),
        ]);
        if (!mounted) return;
        setOrders(orderPage.content);
        setTotalPages(orderPage.totalPages || 1);
        setReceipts(receiptPage.content);
      } catch (err) {
        if (!mounted) return;
        setOrders([]);
        setReceipts([]);
        setTotalPages(1);
        setError(err instanceof Error ? err.message : '생산 주문을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => {
      mounted = false;
    };
  }, [statusFilter, searchKeyword, page, reloadKey]);

  const openCreateForm = () => {
    setEditingOrder(null);
    setForm({
      ...emptyForm,
      warehouseId: warehouses[0] ? String(warehouses[0].warehouseId) : '',
      items: [{ skuId: '', plannedQuantity: '1' }],
    });
    setFormOpen(true);
  };

  const openEditForm = async (order: ApiProductionOrder) => {
    try {
      const detail = await productionService.getOrder(order.id);
      setEditingOrder(detail);
      setForm({
        warehouseId: String(detail.warehouseId),
        memo: detail.memo ?? '',
        items: (detail.items ?? []).map((item) => ({
          skuId: String(item.skuId),
          plannedQuantity: String(item.plannedQuantity),
        })),
      });
      setFormOpen(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : '생산 주문 상세를 불러오지 못했습니다.');
    }
  };

  const showDetail = async (order: ApiProductionOrder) => {
    try {
      setSelectedOrder(await productionService.getOrder(order.id));
    } catch (err) {
      setError(err instanceof Error ? err.message : '생산 주문 상세를 불러오지 못했습니다.');
    }
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canManageProduction) {
      setError('생산 주문을 저장할 권한이 없습니다.');
      return;
    }
    const payload = buildPayload(form);
    if (!payload) return;
    try {
      if (editingOrder) {
        await productionService.updateOrder(editingOrder.id, payload);
        setMessage('생산 주문이 수정되었습니다.');
      } else {
        await productionService.createOrder(payload);
        setMessage('생산 주문이 생성되었습니다.');
      }
      setFormOpen(false);
      setEditingOrder(null);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '생산 주문 저장에 실패했습니다.');
    }
  };

  const buildPayload = (value: OrderForm) => {
    const warehouseId = Number(value.warehouseId);
    if (!warehouseId || Number.isNaN(warehouseId)) {
      setError('입고 창고를 선택해주세요.');
      return null;
    }
    const items = value.items.map((item) => ({
      skuId: Number(item.skuId),
      plannedQuantity: Number(item.plannedQuantity),
    }));
    if (items.some((item) => !item.skuId || Number.isNaN(item.skuId) || item.plannedQuantity <= 0 || Number.isNaN(item.plannedQuantity))) {
      setError('SKU ID와 예정 수량을 올바르게 입력해주세요.');
      return null;
    }
    return {
      warehouseId,
      memo: value.memo.trim() || undefined,
      items,
    };
  };

  const handleStart = async (order: ApiProductionOrder) => {
    if (!canManageProduction) return setError('생산 주문을 시작할 권한이 없습니다.');
    try {
      await productionService.startOrder(order.id);
      setMessage('생산 주문을 시작했습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '생산 시작 처리에 실패했습니다.');
    }
  };

  const handleCancel = async (order: ApiProductionOrder) => {
    if (!canManageProduction) return setError('생산 주문을 취소할 권한이 없습니다.');
    const memo = prompt('취소 사유를 입력해주세요.');
    if (memo === null) return;
    try {
      await productionService.cancelOrder(order.id, memo);
      setMessage('생산 주문을 취소했습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '생산 취소 처리에 실패했습니다.');
    }
  };

  const handleComplete = async (order: ApiProductionOrder) => {
    if (!canManageProduction) return setError('생산 주문을 완료할 권한이 없습니다.');
    try {
      const detail = await productionService.getOrder(order.id);
      const items = (detail.items ?? []).map((item) => {
        const input = prompt(`${item.skuCode} 완료 수량을 입력해주세요. 예정: ${item.plannedQuantity}`, String(item.plannedQuantity));
        if (input === null) return null;
        return {
          skuId: item.skuId,
          completedQuantity: Number(input),
        };
      });
      if (items.some((item) => item === null)) return;
      if (items.some((item) => !item || Number.isNaN(item.completedQuantity) || item.completedQuantity < 0)) {
        setError('완료 수량을 올바르게 입력해주세요.');
        return;
      }
      await productionService.completeOrder(order.id, { items: items as Array<{ skuId: number; completedQuantity: number }> });
      setMessage('생산 주문을 완료 처리했습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '생산 완료 처리에 실패했습니다.');
    }
  };

  const updateItem = (index: number, field: keyof ItemForm, value: string) => {
    setForm((prev) => ({
      ...prev,
      items: prev.items.map((item, itemIndex) => itemIndex === index ? { ...item, [field]: value } : item),
    }));
  };

  return (
    <AdminLayout title="생산 입고 관리">
      <div className="flex items-center justify-between gap-3 mb-5">
        <div>
          <h1 className="text-xl font-semibold text-[#1a1f2e]">생산 입고 관리</h1>
          <p className="mt-1 text-sm text-[#6f7a8a]">생산 주문을 생성하고 완료 처리 시 SKU, 상품, 창고 재고를 자동 증가합니다.</p>
        </div>
        <Button variant="primary" size="sm" onClick={openCreateForm} disabled={!canManageProduction}>생산 주문 생성</Button>
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex flex-wrap gap-3">
        <input
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          onKeyDown={(event) => { if (event.key === 'Enter') { setSearchKeyword(keyword); setPage(1); } }}
          placeholder="생산번호, SKU, 상품명 검색"
          className="flex-1 min-w-[220px] border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <select
          value={statusFilter}
          onChange={(event) => { setStatusFilter(event.target.value as ProductionOrderStatus | 'ALL'); setPage(1); }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none bg-white"
        >
          <option value="ALL">전체 상태</option>
          <option value="PLANNED">계획</option>
          <option value="IN_PROGRESS">진행 중</option>
          <option value="COMPLETED">완료</option>
          <option value="CANCELLED">취소</option>
        </select>
        <Button variant="outline" size="sm" onClick={() => { setSearchKeyword(keyword); setPage(1); }}>검색</Button>
      </div>

      {message && <div className="mb-3 border border-[#b7dfc1] bg-[#f0fff4] px-4 py-3 text-sm text-[#246b38]">{message}</div>}
      {error && <div className="mb-3 border border-[#f1c7c7] bg-[#fff6f6] px-4 py-3 text-sm text-[#c43a3a]">{error}</div>}

      {loading ? (
        <div className="py-12 text-center text-[#999] text-sm bg-white border border-[#e8eaf0]">생산 주문을 불러오는 중입니다.</div>
      ) : (
        <>
          <DataTable<ApiProductionOrder>
            keyField="id"
            data={orders}
            emptyMessage="등록된 생산 주문이 없습니다."
            columns={[
              { key: 'productionNumber', header: '생산번호' },
              { key: 'status', header: '상태', render: (row) => STATUS_LABEL[row.status] ?? row.status },
              { key: 'warehouseName', header: '창고' },
              { key: 'plannedQuantity', header: '예정 수량', render: (row) => `${row.plannedQuantity}개` },
              { key: 'completedQuantity', header: '완료 수량', render: (row) => `${row.completedQuantity}개` },
              { key: 'createdAt', header: '생성일', render: (row) => formatDate(row.createdAt) },
              {
                key: 'actions',
                header: '관리',
                render: (row) => (
                  <div className="flex flex-wrap gap-2">
                    <Button variant="ghost" size="sm" onClick={() => showDetail(row)}>상세</Button>
                    <Button variant="outline" size="sm" onClick={() => openEditForm(row)} disabled={!canManageProduction || row.status === 'COMPLETED' || row.status === 'CANCELLED'}>수정</Button>
                    <Button variant="ghost" size="sm" onClick={() => handleStart(row)} disabled={!canManageProduction || row.status !== 'PLANNED'}>시작</Button>
                    <Button variant="primary" size="sm" onClick={() => handleComplete(row)} disabled={!canManageProduction || row.status === 'COMPLETED' || row.status === 'CANCELLED'}>완료</Button>
                    <Button variant="danger" size="sm" onClick={() => handleCancel(row)} disabled={!canManageProduction || row.status === 'COMPLETED' || row.status === 'CANCELLED'}>취소</Button>
                  </div>
                ),
              },
            ]}
          />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <div className="mt-8 bg-white border border-[#e8eaf0] p-4">
        <h2 className="text-base font-semibold text-[#1a1f2e] mb-3">최근 생산 입고 이력</h2>
        {receipts.length === 0 ? (
          <p className="py-6 text-sm text-[#999] text-center">생산 입고 이력이 없습니다.</p>
        ) : (
          <div className="space-y-2">
            {receipts.map((receipt) => (
              <div key={receipt.id} className="flex items-center justify-between border border-[#f0f1f5] px-3 py-2 text-sm">
                <div>
                  <p className="font-medium text-[#1a1f2e]">{receipt.productionNumber} / {receipt.skuCode}</p>
                  <p className="text-xs text-[#8a9bb5]">{receipt.productName} · {receipt.warehouseName} · 로그 #{receipt.inventoryLogId ?? '-'}</p>
                </div>
                <span className="font-semibold text-[#267a3d]">{receipt.quantity}개 입고</span>
              </div>
            ))}
          </div>
        )}
      </div>

      {selectedOrder && (
        <div className="mt-4 bg-white border border-[#dfe3ea] p-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-semibold text-[#1a1f2e]">상세: {selectedOrder.productionNumber}</h2>
            <Button variant="ghost" size="sm" onClick={() => setSelectedOrder(null)}>닫기</Button>
          </div>
          <div className="mt-3 grid grid-cols-2 gap-3 text-sm text-[#566171]">
            <p>상태: {STATUS_LABEL[selectedOrder.status]}</p>
            <p>창고: {selectedOrder.warehouseName}</p>
            <p>예정 수량: {selectedOrder.plannedQuantity}개</p>
            <p>완료 수량: {selectedOrder.completedQuantity}개</p>
          </div>
          <div className="mt-4 space-y-2">
            {(selectedOrder.items ?? []).map((item) => (
              <div key={item.id} className="border border-[#f0f1f5] px-3 py-2 text-sm">
                <p className="font-medium">{item.skuCode} · {item.productName}</p>
                <p className="text-[#8a9bb5]">예정 {item.plannedQuantity}개 / 완료 {item.completedQuantity}개 / 바코드 {item.barcode ?? '-'}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {formOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4">
          <div className="w-full max-w-[680px] bg-white border border-[#dfe3ea] shadow-xl">
            <div className="flex items-center justify-between border-b border-[#edf0f5] px-5 py-4">
              <h2 className="text-base font-semibold text-[#1a1f2e]">{editingOrder ? '생산 주문 수정' : '생산 주문 생성'}</h2>
              <button type="button" onClick={() => setFormOpen(false)} className="text-sm text-[#6f7a8a] hover:text-[#1a1f2e]">닫기</button>
            </div>
            <form onSubmit={handleSubmit} className="p-5 space-y-4">
              <label className="block text-sm">
                <span className="block mb-1 text-[#566171]">입고 창고</span>
                <select
                  value={form.warehouseId}
                  onChange={(event) => setForm((prev) => ({ ...prev, warehouseId: event.target.value }))}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e] bg-white"
                >
                  <option value="">창고 선택</option>
                  {warehouses.map((warehouse) => (
                    <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>
                  ))}
                </select>
              </label>
              <label className="block text-sm">
                <span className="block mb-1 text-[#566171]">메모</span>
                <input
                  value={form.memo}
                  onChange={(event) => setForm((prev) => ({ ...prev, memo: event.target.value }))}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]"
                  placeholder="생산 메모"
                />
              </label>
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-[#1a1f2e]">생산 품목</span>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setForm((prev) => ({ ...prev, items: [...prev.items, { skuId: '', plannedQuantity: '1' }] }))}
                  >
                    품목 추가
                  </Button>
                </div>
                {form.items.map((item, index) => (
                  <div key={index} className="grid grid-cols-[1fr_120px_80px] gap-2">
                    <input
                      value={item.skuId}
                      onChange={(event) => updateItem(index, 'skuId', event.target.value)}
                      className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
                      placeholder="SKU ID"
                    />
                    <input
                      type="number"
                      min={1}
                      value={item.plannedQuantity}
                      onChange={(event) => updateItem(index, 'plannedQuantity', event.target.value)}
                      className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
                      placeholder="예정 수량"
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => setForm((prev) => ({ ...prev, items: prev.items.filter((_, itemIndex) => itemIndex !== index) || [{ skuId: '', plannedQuantity: '1' }] }))}
                      disabled={form.items.length <= 1}
                    >
                      삭제
                    </Button>
                  </div>
                ))}
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="ghost" size="sm" onClick={() => setFormOpen(false)}>취소</Button>
                <Button type="submit" variant="primary" size="sm" disabled={!canManageProduction}>저장</Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AdminLayout>
  );
}

function formatDate(value: string | null) {
  if (!value) return '-';
  return new Date(value).toLocaleString('ko-KR');
}
