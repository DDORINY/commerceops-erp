'use client';

import { useCallback, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import { getUserRole } from '@/lib/auth';
import { permissionGroupService } from '@/lib/services/permissionGroupService';
import { skuService, type ApiSku } from '@/lib/services/skuService';
import { stockCountService, type ApiStockCount, type ApiStockCountItem, type ApiStockCountStatus } from '@/lib/services/stockCountService';
import { warehouseService, type ApiWarehouse } from '@/lib/services/warehouseService';

const PAGE_SIZE = 10;

const STATUS_LABEL: Record<ApiStockCountStatus, string> = {
  DRAFT: '임시저장',
  IN_PROGRESS: '진행 중',
  COMPLETED: '완료',
  CANCELLED: '취소',
};

export default function AdminStockCountsPage() {
  const [sessions, setSessions] = useState<ApiStockCount[]>([]);
  const [selected, setSelected] = useState<ApiStockCount | null>(null);
  const [warehouses, setWarehouses] = useState<ApiWarehouse[]>([]);
  const [skus, setSkus] = useState<ApiSku[]>([]);
  const [warehouseId, setWarehouseId] = useState('');
  const [memo, setMemo] = useState('');
  const [skuKeyword, setSkuKeyword] = useState('');
  const [selectedSkuId, setSelectedSkuId] = useState('');
  const [countedQuantity, setCountedQuantity] = useState(0);
  const [itemMemo, setItemMemo] = useState('');
  const [status, setStatus] = useState<ApiStockCountStatus | 'ALL'>('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);

  const role = getUserRole();
  const canManage = role === 'SUPER_ADMIN' || permissionCodes.includes('STOCK_COUNT_MANAGE') || (!permissionCodes.length && role === 'ADMIN');

  const loadSessions = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await stockCountService.getStockCounts(status, page - 1, PAGE_SIZE);
      setSessions(response.content);
      setTotalPages(response.totalPages || 1);
    } catch (err) {
      setSessions([]);
      setTotalPages(1);
      setError(err instanceof Error ? err.message : '재고 실사 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [page, status]);

  useEffect(() => {
    permissionGroupService.getMyEffectivePermissions()
      .then((response) => setPermissionCodes(response.permissionCodes))
      .catch(() => setPermissionCodes([]));

    warehouseService.getWarehouses()
      .then((items) => {
        const active = items.filter((warehouse) => warehouse.active);
        setWarehouses(active);
        if (active[0]) setWarehouseId(String(active[0].warehouseId));
      })
      .catch(() => setWarehouses([]));
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadSessions();
  }, [loadSessions]);

  useEffect(() => {
    skuService.getSkus({ keyword: skuKeyword || undefined, active: true, page: 0, size: 20 })
      .then((response) => {
        setSkus(response.content);
        if (!selectedSkuId && response.content[0]) setSelectedSkuId(String(response.content[0].id));
      })
      .catch(() => setSkus([]));
  }, [selectedSkuId, skuKeyword]);

  const loadDetail = async (stockCountId: number) => {
    setError('');
    try {
      setSelected(await stockCountService.getStockCount(stockCountId));
    } catch (err) {
      setError(err instanceof Error ? err.message : '재고 실사 상세를 불러오지 못했습니다.');
    }
  };

  const createSession = async () => {
    if (!canManage || !warehouseId) {
      setError('재고 실사를 생성할 권한 또는 창고가 없습니다.');
      return;
    }
    setProcessing(true);
    setError('');
    setMessage('');
    try {
      const result = await stockCountService.createStockCount(Number(warehouseId), memo || undefined);
      setSelected(result);
      setMessage('재고 실사 세션을 생성했습니다.');
      setMemo('');
      await loadSessions();
    } catch (err) {
      setError(err instanceof Error ? err.message : '재고 실사 생성에 실패했습니다.');
    } finally {
      setProcessing(false);
    }
  };

  const addOrUpdateItem = async () => {
    if (!selected || !selectedSkuId) return;
    setProcessing(true);
    setError('');
    setMessage('');
    try {
      const result = await stockCountService.updateItems(selected.stockCountId, [{
        skuId: Number(selectedSkuId),
        countedQuantity,
        memo: itemMemo || undefined,
      }]);
      setSelected(result);
      setMessage('실사 품목을 저장했습니다.');
      setItemMemo('');
    } catch (err) {
      setError(err instanceof Error ? err.message : '실사 품목 저장에 실패했습니다.');
    } finally {
      setProcessing(false);
    }
  };

  const changeStatus = async (action: 'start' | 'complete' | 'cancel') => {
    if (!selected) return;
    setProcessing(true);
    setError('');
    setMessage('');
    try {
      const result = action === 'start'
        ? await stockCountService.start(selected.stockCountId)
        : action === 'complete'
          ? await stockCountService.complete(selected.stockCountId)
          : await stockCountService.cancel(selected.stockCountId);
      setSelected(result);
      setMessage(action === 'start' ? '재고 실사를 시작했습니다.' : action === 'complete' ? '재고 실사를 완료했습니다.' : '재고 실사를 취소했습니다.');
      await loadSessions();
    } catch (err) {
      setError(err instanceof Error ? err.message : '재고 실사 상태 변경에 실패했습니다.');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <AdminLayout title="재고 실사">
      <div className="mb-5">
        <h1 className="text-xl font-semibold text-[#1a1f2e]">재고 실사</h1>
        <p className="mt-1 text-sm text-[#6f7a8a]">창고별 실사 세션을 만들고 시스템 재고와 실사 수량 차이를 조정합니다.</p>
      </div>

      {message && <div className="mb-3 border border-[#b7dfc1] bg-[#f0fff4] px-4 py-3 text-sm text-[#246b38]">{message}</div>}
      {error && <div className="mb-3 border border-[#f1c7c7] bg-[#fff6f6] px-4 py-3 text-sm text-[#c43a3a]">{error}</div>}

      <div className="mb-4 grid gap-4 lg:grid-cols-[360px_1fr]">
        <div className="border border-[#e8eaf0] bg-white p-4">
          <h2 className="mb-3 text-base font-semibold text-[#1a1f2e]">세션 생성</h2>
          <div className="space-y-3">
            <select value={warehouseId} onChange={(event) => setWarehouseId(event.target.value)} className="w-full border border-[#dfe3ea] px-3 py-2 text-sm">
              {warehouses.map((warehouse) => (
                <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>
              ))}
            </select>
            <input value={memo} onChange={(event) => setMemo(event.target.value)} placeholder="실사 메모" className="w-full border border-[#dfe3ea] px-3 py-2 text-sm" />
            <Button variant="primary" size="sm" onClick={createSession} disabled={processing || !canManage}>실사 생성</Button>
          </div>
          {!canManage && <p className="mt-2 text-xs text-[#c43a3a]">현재 계정은 재고 실사 관리 권한이 없습니다.</p>}
        </div>

        <div className="border border-[#e8eaf0] bg-white p-4">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-base font-semibold text-[#1a1f2e]">실사 목록</h2>
            <select value={status} onChange={(event) => { setStatus(event.target.value as ApiStockCountStatus | 'ALL'); setPage(1); }} className="border border-[#dfe3ea] px-3 py-2 text-sm">
              <option value="ALL">전체</option>
              <option value="DRAFT">임시저장</option>
              <option value="IN_PROGRESS">진행 중</option>
              <option value="COMPLETED">완료</option>
              <option value="CANCELLED">취소</option>
            </select>
          </div>
          {loading ? (
            <div className="py-10 text-center text-sm text-[#999]">재고 실사 목록을 불러오는 중입니다.</div>
          ) : (
            <>
              <DataTable<ApiStockCount>
                keyField="stockCountId"
                data={sessions}
                emptyMessage="재고 실사 세션이 없습니다."
                columns={[
                  { key: 'countNumber', header: '실사번호' },
                  { key: 'warehouseName', header: '창고' },
                  { key: 'status', header: '상태', render: (row) => STATUS_LABEL[row.status] },
                  { key: 'createdAt', header: '생성일', render: (row) => row.createdAt?.slice(0, 10) },
                  { key: 'actions', header: '관리', render: (row) => <Button variant="outline" size="sm" onClick={() => loadDetail(row.stockCountId)}>상세</Button> },
                ]}
              />
              <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            </>
          )}
        </div>
      </div>

      {selected && (
        <div className="border border-[#e8eaf0] bg-white p-4">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-semibold text-[#1a1f2e]">{selected.countNumber}</h2>
              <p className="mt-1 text-sm text-[#6f7a8a]">{selected.warehouseName} · {STATUS_LABEL[selected.status]}</p>
            </div>
            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={() => changeStatus('start')} disabled={processing || !canManage || selected.status !== 'DRAFT'}>시작</Button>
              <Button variant="primary" size="sm" onClick={() => changeStatus('complete')} disabled={processing || !canManage || selected.status === 'COMPLETED' || selected.status === 'CANCELLED'}>완료/조정</Button>
              <Button variant="outline" size="sm" onClick={() => changeStatus('cancel')} disabled={processing || !canManage || selected.status === 'COMPLETED' || selected.status === 'CANCELLED'}>취소</Button>
            </div>
          </div>

          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_180px_160px_1fr_auto]">
            <input value={skuKeyword} onChange={(event) => setSkuKeyword(event.target.value)} placeholder="SKU 검색" className="border border-[#dfe3ea] px-3 py-2 text-sm" />
            <select value={selectedSkuId} onChange={(event) => setSelectedSkuId(event.target.value)} className="border border-[#dfe3ea] px-3 py-2 text-sm">
              {skus.map((sku) => (
                <option key={sku.id} value={sku.id}>{sku.skuCode}</option>
              ))}
            </select>
            <input type="number" min={0} value={countedQuantity} onChange={(event) => setCountedQuantity(Math.max(0, Number(event.target.value)))} className="border border-[#dfe3ea] px-3 py-2 text-sm" />
            <input value={itemMemo} onChange={(event) => setItemMemo(event.target.value)} placeholder="품목 메모" className="border border-[#dfe3ea] px-3 py-2 text-sm" />
            <Button variant="outline" size="sm" onClick={addOrUpdateItem} disabled={processing || !canManage || selected.status === 'COMPLETED' || selected.status === 'CANCELLED'}>품목 저장</Button>
          </div>

          <DataTable<ApiStockCountItem>
            keyField="itemId"
            data={selected.items}
            emptyMessage="실사 품목이 없습니다."
            columns={[
              { key: 'skuCode', header: 'SKU' },
              { key: 'productName', header: '상품명' },
              { key: 'systemQuantity', header: '시스템', render: (row) => row.systemQuantity.toLocaleString() },
              { key: 'countedQuantity', header: '실사', render: (row) => row.countedQuantity?.toLocaleString() ?? '-' },
              { key: 'differenceQuantity', header: '차이', render: (row) => row.differenceQuantity.toLocaleString() },
              { key: 'memo', header: '메모', render: (row) => row.memo || '-' },
            ]}
          />
        </div>
      )}
    </AdminLayout>
  );
}
