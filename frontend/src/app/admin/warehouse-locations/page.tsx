'use client';

import { useCallback, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import Pagination from '@/components/common/Pagination';
import { getUserRole } from '@/lib/auth';
import { permissionGroupService } from '@/lib/services/permissionGroupService';
import { warehouseService, type ApiWarehouse } from '@/lib/services/warehouseService';
import {
  warehouseLocationService,
  type ApiWarehouseLocation,
  type ApiWarehouseLocationStock,
} from '@/lib/services/warehouseLocationService';

const PAGE_SIZE = 10;

type ActiveFilter = 'ALL' | 'true' | 'false';

const emptyForm = {
  warehouseId: '',
  code: '',
  name: '',
  zone: '',
  aisle: '',
  rack: '',
  cell: '',
};

export default function AdminWarehouseLocationsPage() {
  const [warehouses, setWarehouses] = useState<ApiWarehouse[]>([]);
  const [locations, setLocations] = useState<ApiWarehouseLocation[]>([]);
  const [selectedLocation, setSelectedLocation] = useState<ApiWarehouseLocation | null>(null);
  const [stocks, setStocks] = useState<ApiWarehouseLocationStock[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [keyword, setKeyword] = useState('');
  const [warehouseFilter, setWarehouseFilter] = useState('');
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [stockPage, setStockPage] = useState(1);
  const [stockTotalPages, setStockTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [stockLoading, setStockLoading] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);

  const role = getUserRole();
  const canManage = role === 'SUPER_ADMIN' || permissionCodes.includes('WAREHOUSE_MANAGE') || (!permissionCodes.length && role === 'ADMIN');

  const loadLocations = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await warehouseLocationService.getLocations({
        warehouseId: warehouseFilter ? Number(warehouseFilter) : undefined,
        active: activeFilter === 'ALL' ? 'ALL' : activeFilter === 'true',
        keyword: keyword || undefined,
        page: page - 1,
        size: PAGE_SIZE,
      });
      setLocations(response.content);
      setTotalPages(response.totalPages || 1);
    } catch (err) {
      setLocations([]);
      setTotalPages(1);
      setError(err instanceof Error ? err.message : '창고 위치 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [activeFilter, keyword, page, warehouseFilter]);

  const loadStocks = useCallback(async (locationId: number, nextPage = stockPage) => {
    setStockLoading(true);
    try {
      const response = await warehouseLocationService.getLocationStocks(locationId, nextPage - 1, PAGE_SIZE);
      setStocks(response.content);
      setStockTotalPages(response.totalPages || 1);
    } catch (err) {
      setStocks([]);
      setStockTotalPages(1);
      setError(err instanceof Error ? err.message : '위치별 재고를 불러오지 못했습니다.');
    } finally {
      setStockLoading(false);
    }
  }, [stockPage]);

  useEffect(() => {
    permissionGroupService.getMyEffectivePermissions()
      .then((response) => setPermissionCodes(response.permissionCodes))
      .catch(() => setPermissionCodes([]));

    warehouseService.getWarehouses()
      .then((items) => {
        const activeWarehouses = items.filter((warehouse) => warehouse.active);
        setWarehouses(activeWarehouses);
        if (activeWarehouses[0]) {
          setForm((prev) => ({ ...prev, warehouseId: String(activeWarehouses[0].warehouseId) }));
        }
      })
      .catch(() => setWarehouses([]));
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadLocations();
  }, [loadLocations]);

  useEffect(() => {
    if (selectedLocation) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      loadStocks(selectedLocation.locationId, stockPage);
    }
  }, [loadStocks, selectedLocation, stockPage]);

  const resetForm = () => {
    setSelectedLocation(null);
    setStocks([]);
    setStockPage(1);
    setForm({
      ...emptyForm,
      warehouseId: warehouses[0] ? String(warehouses[0].warehouseId) : '',
    });
  };

  const openEdit = (location: ApiWarehouseLocation) => {
    setSelectedLocation(location);
    setStockPage(1);
    setForm({
      warehouseId: String(location.warehouseId),
      code: location.code,
      name: location.name,
      zone: location.zone ?? '',
      aisle: location.aisle ?? '',
      rack: location.rack ?? '',
      cell: location.cell ?? '',
    });
  };

  const saveLocation = async () => {
    if (!canManage) {
      setError('창고 위치를 관리할 권한이 없습니다.');
      return;
    }
    if (!form.warehouseId || !form.code.trim() || !form.name.trim()) {
      setError('창고, 위치 코드, 위치명을 입력해주세요.');
      return;
    }
    setProcessing(true);
    setError('');
    setMessage('');
    const payload = {
      warehouseId: Number(form.warehouseId),
      code: form.code.trim(),
      name: form.name.trim(),
      zone: form.zone.trim() || undefined,
      aisle: form.aisle.trim() || undefined,
      rack: form.rack.trim() || undefined,
      cell: form.cell.trim() || undefined,
    };
    try {
      const result = selectedLocation
        ? await warehouseLocationService.updateLocation(selectedLocation.locationId, payload)
        : await warehouseLocationService.createLocation(payload);
      setMessage(selectedLocation ? '창고 위치가 수정되었습니다.' : '창고 위치가 생성되었습니다.');
      setSelectedLocation(result);
      await loadLocations();
    } catch (err) {
      setError(err instanceof Error ? err.message : '창고 위치 저장에 실패했습니다.');
    } finally {
      setProcessing(false);
    }
  };

  const toggleActive = async (location: ApiWarehouseLocation) => {
    if (!canManage) {
      setError('창고 위치 활성 상태를 변경할 권한이 없습니다.');
      return;
    }
    setProcessing(true);
    setError('');
    setMessage('');
    try {
      const result = await warehouseLocationService.updateActive(location.locationId, !location.active);
      setMessage(`창고 위치가 ${result.active ? '활성' : '비활성'} 처리되었습니다.`);
      if (selectedLocation?.locationId === result.locationId) setSelectedLocation(result);
      await loadLocations();
    } catch (err) {
      setError(err instanceof Error ? err.message : '창고 위치 활성 상태 변경에 실패했습니다.');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <AdminLayout title="창고 위치 관리">
      <div className="mb-5">
        <h1 className="text-xl font-semibold text-[#1a1f2e]">창고 위치 관리</h1>
        <p className="mt-1 text-sm text-[#6f7a8a]">
          창고 내부 위치를 코드 단위로 관리하고 위치별 SKU 재고 기반을 확인합니다.
        </p>
      </div>

      {message && <div className="mb-3 border border-[#b7dfc1] bg-[#f0fff4] px-4 py-3 text-sm text-[#246b38]">{message}</div>}
      {error && <div className="mb-3 border border-[#f1c7c7] bg-[#fff6f6] px-4 py-3 text-sm text-[#c43a3a]">{error}</div>}

      <div className="mb-4 grid gap-4 xl:grid-cols-[380px_1fr]">
        <section className="border border-[#e8eaf0] bg-white p-4">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-base font-semibold text-[#1a1f2e]">{selectedLocation ? '위치 수정' : '위치 생성'}</h2>
            <Button variant="outline" size="sm" onClick={resetForm}>새 위치</Button>
          </div>
          <div className="space-y-3">
            <select
              value={form.warehouseId}
              onChange={(event) => setForm((prev) => ({ ...prev, warehouseId: event.target.value }))}
              className="w-full border border-[#dfe3ea] px-3 py-2 text-sm"
              disabled={Boolean(selectedLocation)}
            >
              <option value="">창고 선택</option>
              {warehouses.map((warehouse) => (
                <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>
              ))}
            </select>
            <input className="w-full border border-[#dfe3ea] px-3 py-2 text-sm" placeholder="위치 코드 (예: A-01-01)" value={form.code} onChange={(event) => setForm((prev) => ({ ...prev, code: event.target.value }))} />
            <input className="w-full border border-[#dfe3ea] px-3 py-2 text-sm" placeholder="위치명" value={form.name} onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))} />
            <div className="grid grid-cols-2 gap-2">
              <input className="border border-[#dfe3ea] px-3 py-2 text-sm" placeholder="구역" value={form.zone} onChange={(event) => setForm((prev) => ({ ...prev, zone: event.target.value }))} />
              <input className="border border-[#dfe3ea] px-3 py-2 text-sm" placeholder="통로" value={form.aisle} onChange={(event) => setForm((prev) => ({ ...prev, aisle: event.target.value }))} />
              <input className="border border-[#dfe3ea] px-3 py-2 text-sm" placeholder="랙" value={form.rack} onChange={(event) => setForm((prev) => ({ ...prev, rack: event.target.value }))} />
              <input className="border border-[#dfe3ea] px-3 py-2 text-sm" placeholder="셀" value={form.cell} onChange={(event) => setForm((prev) => ({ ...prev, cell: event.target.value }))} />
            </div>
            <Button variant="primary" size="sm" onClick={saveLocation} disabled={processing || !canManage}>
              {selectedLocation ? '수정 저장' : '위치 생성'}
            </Button>
            {!canManage && <p className="text-xs text-[#c43a3a]">현재 계정에는 창고 위치 관리 권한이 없습니다.</p>}
          </div>
        </section>

        <section className="border border-[#e8eaf0] bg-white p-4">
          <div className="mb-3 flex flex-wrap items-center gap-2">
            <input
              value={keyword}
              onChange={(event) => { setKeyword(event.target.value); setPage(1); }}
              placeholder="코드, 위치명, 구역 검색"
              className="min-w-[220px] flex-1 border border-[#dfe3ea] px-3 py-2 text-sm"
            />
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
            <div className="py-10 text-center text-sm text-[#999]">창고 위치 목록을 불러오는 중입니다.</div>
          ) : (
            <>
              <DataTable<ApiWarehouseLocation>
                keyField="locationId"
                data={locations}
                emptyMessage="등록된 창고 위치가 없습니다."
                columns={[
                  { key: 'code', header: '위치 코드' },
                  { key: 'name', header: '위치명' },
                  { key: 'warehouseName', header: '창고' },
                  { key: 'zone', header: '구역', render: (row) => row.zone || '-' },
                  { key: 'path', header: '상세 위치', render: (row) => [row.aisle, row.rack, row.cell].filter(Boolean).join(' / ') || '-' },
                  { key: 'active', header: '상태', render: (row) => row.active ? '활성' : '비활성' },
                  {
                    key: 'actions',
                    header: '관리',
                    render: (row) => (
                      <div className="flex gap-2">
                        <Button variant="outline" size="sm" onClick={() => openEdit(row)}>상세</Button>
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

      {selectedLocation && (
        <section className="border border-[#e8eaf0] bg-white p-4">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-semibold text-[#1a1f2e]">위치별 재고</h2>
              <p className="mt-1 text-sm text-[#6f7a8a]">
                {selectedLocation.warehouseName} · {selectedLocation.code} / {selectedLocation.name}
              </p>
            </div>
            <Button variant="outline" size="sm" onClick={() => loadStocks(selectedLocation.locationId, stockPage)} disabled={stockLoading}>
              새로고침
            </Button>
          </div>

          {stockLoading ? (
            <div className="py-8 text-center text-sm text-[#999]">위치별 재고를 불러오는 중입니다.</div>
          ) : (
            <>
              <DataTable<ApiWarehouseLocationStock>
                keyField="stockId"
                data={stocks}
                emptyMessage="이 위치에 등록된 SKU 재고가 없습니다."
                columns={[
                  { key: 'skuCode', header: 'SKU' },
                  { key: 'barcode', header: '바코드', render: (row) => row.barcode || '-' },
                  { key: 'productName', header: '상품명' },
                  { key: 'quantity', header: '수량', render: (row) => row.quantity.toLocaleString() },
                  { key: 'reservedQuantity', header: '예약', render: (row) => row.reservedQuantity.toLocaleString() },
                  { key: 'availableQuantity', header: '가용', render: (row) => row.availableQuantity.toLocaleString() },
                  { key: 'updatedAt', header: '수정일', render: (row) => row.updatedAt?.slice(0, 10) ?? '-' },
                ]}
              />
              <Pagination currentPage={stockPage} totalPages={stockTotalPages} onPageChange={setStockPage} />
            </>
          )}
          <p className="mt-3 text-xs text-[#6f7a8a]">
            위치별 재고 수량 변경과 이동 처리는 v0.5.7 재고 이동 고도화에서 확장합니다.
          </p>
        </section>
      )}
    </AdminLayout>
  );
}
