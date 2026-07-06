'use client';

import { useCallback, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import Pagination from '@/components/common/Pagination';
import { getUserRole } from '@/lib/auth';
import { inventoryAlertService, type ApiInventoryAlertRule, type ApiLowStockAlert } from '@/lib/services/inventoryAlertService';
import { permissionGroupService } from '@/lib/services/permissionGroupService';
import { skuService, type ApiSku } from '@/lib/services/skuService';
import { warehouseService, type ApiWarehouse } from '@/lib/services/warehouseService';

const PAGE_SIZE = 10;

type ActiveFilter = 'ALL' | 'true' | 'false';

const emptyForm = {
  skuId: '',
  warehouseId: '',
  thresholdQuantity: '0',
  memo: '',
};

export default function AdminInventoryAlertsPage() {
  const [rules, setRules] = useState<ApiInventoryAlertRule[]>([]);
  const [alerts, setAlerts] = useState<ApiLowStockAlert[]>([]);
  const [warehouses, setWarehouses] = useState<ApiWarehouse[]>([]);
  const [skus, setSkus] = useState<ApiSku[]>([]);
  const [selectedRule, setSelectedRule] = useState<ApiInventoryAlertRule | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [keyword, setKeyword] = useState('');
  const [skuKeyword, setSkuKeyword] = useState('');
  const [warehouseFilter, setWarehouseFilter] = useState('');
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);

  const role = getUserRole();
  const canManage = role === 'SUPER_ADMIN' || permissionCodes.includes('INVENTORY_WRITE') || (!permissionCodes.length && role === 'ADMIN');

  const loadRules = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await inventoryAlertService.getRules({
        warehouseId: warehouseFilter ? Number(warehouseFilter) : undefined,
        active: activeFilter === 'ALL' ? 'ALL' : activeFilter === 'true',
        keyword: keyword || undefined,
        page: page - 1,
        size: PAGE_SIZE,
      });
      setRules(response.content);
      setTotalPages(response.totalPages || 1);
    } catch (err) {
      setRules([]);
      setTotalPages(1);
      setError(err instanceof Error ? err.message : '안전재고 기준을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [activeFilter, keyword, page, warehouseFilter]);

  const loadAlerts = useCallback(async () => {
    try {
      setAlerts(await inventoryAlertService.getLowStockAlerts(warehouseFilter ? Number(warehouseFilter) : undefined));
    } catch {
      setAlerts([]);
    }
  }, [warehouseFilter]);

  useEffect(() => {
    permissionGroupService.getMyEffectivePermissions()
      .then((response) => setPermissionCodes(response.permissionCodes))
      .catch(() => setPermissionCodes([]));

    warehouseService.getWarehouses()
      .then((items) => setWarehouses(items.filter((warehouse) => warehouse.active)))
      .catch(() => setWarehouses([]));
  }, []);

  useEffect(() => {
    skuService.getSkus({ keyword: skuKeyword || undefined, active: true, page: 0, size: 20 })
      .then((response) => {
        setSkus(response.content);
        if (!form.skuId && response.content[0]) {
          setForm((prev) => ({ ...prev, skuId: String(response.content[0].id) }));
        }
      })
      .catch(() => setSkus([]));
  }, [form.skuId, skuKeyword]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadRules();
  }, [loadRules]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadAlerts();
  }, [loadAlerts]);

  const resetForm = () => {
    setSelectedRule(null);
    setForm({
      ...emptyForm,
      skuId: skus[0] ? String(skus[0].id) : '',
    });
  };

  const openEdit = (rule: ApiInventoryAlertRule) => {
    setSelectedRule(rule);
    setForm({
      skuId: String(rule.skuId),
      warehouseId: rule.warehouseId ? String(rule.warehouseId) : '',
      thresholdQuantity: String(rule.thresholdQuantity),
      memo: rule.memo ?? '',
    });
  };

  const saveRule = async () => {
    if (!canManage) {
      setError('안전재고 기준을 관리할 권한이 없습니다.');
      return;
    }
    const skuId = Number(form.skuId);
    const thresholdQuantity = Number(form.thresholdQuantity);
    if (!skuId || Number.isNaN(thresholdQuantity) || thresholdQuantity < 0) {
      setError('SKU와 0 이상의 안전재고 기준을 입력해주세요.');
      return;
    }
    setProcessing(true);
    setError('');
    setMessage('');
    const payload = {
      skuId,
      warehouseId: form.warehouseId ? Number(form.warehouseId) : null,
      thresholdQuantity,
      memo: form.memo.trim() || undefined,
    };
    try {
      const result = selectedRule
        ? await inventoryAlertService.updateRule(selectedRule.ruleId, payload)
        : await inventoryAlertService.createRule(payload);
      setSelectedRule(result);
      setMessage(selectedRule ? '안전재고 기준이 수정되었습니다.' : '안전재고 기준이 생성되었습니다.');
      await Promise.all([loadRules(), loadAlerts()]);
    } catch (err) {
      setError(err instanceof Error ? err.message : '안전재고 기준 저장에 실패했습니다.');
    } finally {
      setProcessing(false);
    }
  };

  const toggleActive = async (rule: ApiInventoryAlertRule) => {
    if (!canManage) {
      setError('안전재고 기준 활성 상태를 변경할 권한이 없습니다.');
      return;
    }
    setProcessing(true);
    setError('');
    setMessage('');
    try {
      const result = await inventoryAlertService.updateActive(rule.ruleId, !rule.active);
      if (selectedRule?.ruleId === result.ruleId) setSelectedRule(result);
      setMessage(`안전재고 기준이 ${result.active ? '활성' : '비활성'} 처리되었습니다.`);
      await Promise.all([loadRules(), loadAlerts()]);
    } catch (err) {
      setError(err instanceof Error ? err.message : '안전재고 기준 활성 상태 변경에 실패했습니다.');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <AdminLayout title="안전재고 알림">
      <div className="mb-5">
        <h1 className="text-xl font-semibold text-[#1a1f2e]">안전재고 알림</h1>
        <p className="mt-1 text-sm text-[#6f7a8a]">SKU 또는 SKU+창고 단위의 안전재고 기준을 관리하고 재고 부족 항목을 확인합니다.</p>
      </div>

      {message && <div className="mb-3 border border-[#b7dfc1] bg-[#f0fff4] px-4 py-3 text-sm text-[#246b38]">{message}</div>}
      {error && <div className="mb-3 border border-[#f1c7c7] bg-[#fff6f6] px-4 py-3 text-sm text-[#c43a3a]">{error}</div>}

      <div className="mb-4 grid gap-4 xl:grid-cols-[380px_1fr]">
        <section className="border border-[#e8eaf0] bg-white p-4">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-base font-semibold text-[#1a1f2e]">{selectedRule ? '기준 수정' : '기준 생성'}</h2>
            <Button variant="outline" size="sm" onClick={resetForm}>새 기준</Button>
          </div>
          <div className="space-y-3">
            <input value={skuKeyword} onChange={(event) => setSkuKeyword(event.target.value)} placeholder="SKU 검색" className="w-full border border-[#dfe3ea] px-3 py-2 text-sm" />
            <select value={form.skuId} onChange={(event) => setForm((prev) => ({ ...prev, skuId: event.target.value }))} className="w-full border border-[#dfe3ea] px-3 py-2 text-sm" disabled={Boolean(selectedRule)}>
              <option value="">SKU 선택</option>
              {skus.map((sku) => (
                <option key={sku.id} value={sku.id}>{sku.skuCode} · {sku.productName}</option>
              ))}
            </select>
            <select value={form.warehouseId} onChange={(event) => setForm((prev) => ({ ...prev, warehouseId: event.target.value }))} className="w-full border border-[#dfe3ea] px-3 py-2 text-sm">
              <option value="">전체 창고 기준</option>
              {warehouses.map((warehouse) => (
                <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>
              ))}
            </select>
            <input type="number" min={0} value={form.thresholdQuantity} onChange={(event) => setForm((prev) => ({ ...prev, thresholdQuantity: event.target.value }))} placeholder="안전재고 기준 수량" className="w-full border border-[#dfe3ea] px-3 py-2 text-sm" />
            <input value={form.memo} onChange={(event) => setForm((prev) => ({ ...prev, memo: event.target.value }))} placeholder="메모" className="w-full border border-[#dfe3ea] px-3 py-2 text-sm" />
            <Button variant="primary" size="sm" onClick={saveRule} disabled={processing || !canManage}>
              {selectedRule ? '수정 저장' : '기준 생성'}
            </Button>
            {!canManage && <p className="text-xs text-[#c43a3a]">현재 계정에는 안전재고 기준 관리 권한이 없습니다.</p>}
          </div>
        </section>

        <section className="border border-[#e8eaf0] bg-white p-4">
          <div className="mb-3 flex flex-wrap items-center gap-2">
            <input value={keyword} onChange={(event) => { setKeyword(event.target.value); setPage(1); }} placeholder="SKU, 바코드, 상품명 검색" className="min-w-[220px] flex-1 border border-[#dfe3ea] px-3 py-2 text-sm" />
            <select value={warehouseFilter} onChange={(event) => { setWarehouseFilter(event.target.value); setPage(1); }} className="border border-[#dfe3ea] px-3 py-2 text-sm">
              <option value="">전체 창고</option>
              {warehouses.map((warehouse) => (
                <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>
              ))}
            </select>
            <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value as ActiveFilter); setPage(1); }} className="border border-[#dfe3ea] px-3 py-2 text-sm">
              <option value="ALL">전체 상태</option>
              <option value="true">활성</option>
              <option value="false">비활성</option>
            </select>
          </div>

          {loading ? (
            <div className="py-10 text-center text-sm text-[#999]">안전재고 기준을 불러오는 중입니다.</div>
          ) : (
            <>
              <DataTable<ApiInventoryAlertRule>
                keyField="ruleId"
                data={rules}
                emptyMessage="등록된 안전재고 기준이 없습니다."
                columns={[
                  { key: 'skuCode', header: 'SKU' },
                  { key: 'productName', header: '상품명' },
                  { key: 'warehouseName', header: '창고' },
                  { key: 'thresholdQuantity', header: '기준', render: (row) => row.thresholdQuantity.toLocaleString() },
                  { key: 'active', header: '상태', render: (row) => row.active ? '활성' : '비활성' },
                  {
                    key: 'actions',
                    header: '관리',
                    render: (row) => (
                      <div className="flex gap-2">
                        <Button variant="outline" size="sm" onClick={() => openEdit(row)}>수정</Button>
                        <Button variant="outline" size="sm" onClick={() => toggleActive(row)} disabled={processing || !canManage}>
                          {row.active ? '비활성' : '활성'}
                        </Button>
                      </div>
                    ),
                  },
                ]}
              />
              <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            </>
          )}
        </section>
      </div>

      <section className="border border-[#e8eaf0] bg-white p-4">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-base font-semibold text-[#1a1f2e]">재고 부족 항목</h2>
          <Button variant="outline" size="sm" onClick={loadAlerts}>새로고침</Button>
        </div>
        <DataTable<ApiLowStockAlert>
          keyField="ruleId"
          data={alerts}
          emptyMessage="현재 안전재고 기준 이하인 항목이 없습니다."
          columns={[
            { key: 'skuCode', header: 'SKU' },
            { key: 'productName', header: '상품명' },
            { key: 'warehouseName', header: '창고' },
            { key: 'currentQuantity', header: '현재', render: (row) => row.currentQuantity.toLocaleString() },
            { key: 'thresholdQuantity', header: '기준', render: (row) => row.thresholdQuantity.toLocaleString() },
            { key: 'shortageQuantity', header: '부족', render: (row) => row.shortageQuantity.toLocaleString() },
            { key: 'memo', header: '메모', render: (row) => row.memo || '-' },
          ]}
        />
      </section>
    </AdminLayout>
  );
}
