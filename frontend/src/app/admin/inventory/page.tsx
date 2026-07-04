'use client';

import { useState, useEffect } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import StatCard from '@/components/admin/StatCard';
import { adminService, type ApiInventoryItem } from '@/lib/services/adminService';
import { INVENTORY_STATUS_LABEL, INVENTORY_STATUS_COLOR, downloadCsv } from '@/lib/format';
import { warehouseService, type ApiWarehouse } from '@/lib/services/warehouseService';

const PAGE_SIZE = 8;

export default function AdminInventoryPage() {
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [inventory, setInventory] = useState<ApiInventoryItem[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [warehouses, setWarehouses] = useState<ApiWarehouse[]>([]);
  const [workWarehouseId, setWorkWarehouseId] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [stockCounts, setStockCounts] = useState({
    normal: 0,
    low: 0,
    out: 0,
  });

  useEffect(() => {
    let mounted = true;

    const loadInventory = async () => {
      setLoading(true);
      setError('');

      try {
        const [list, normal, low, out] = await Promise.all([
          adminService.getInventory({
            keyword: searchKeyword || undefined,
            status: statusFilter,
            page: page - 1,
            size: PAGE_SIZE,
          }),
          adminService.getInventory({ status: 'NORMAL', page: 0, size: 1 }),
          adminService.getInventory({ status: 'LOW_STOCK', page: 0, size: 1 }),
          adminService.getInventory({ status: 'OUT_OF_STOCK', page: 0, size: 1 }),
        ]);
        if (!mounted) return;
        setInventory(list.content);
        setTotalPages(list.totalPages || 1);
        setTotalElements(list.totalElements);
        setStockCounts({
          normal: normal.totalElements,
          low: low.totalElements,
          out: out.totalElements,
        });
      } catch (err) {
        if (!mounted) return;
        setInventory([]);
        setTotalPages(1);
        setTotalElements(0);
        setError(err instanceof Error ? err.message : '재고 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadInventory();

    return () => {
      mounted = false;
    };
  }, [statusFilter, searchKeyword, page, reloadKey]);

  useEffect(() => {
    warehouseService.getWarehouses().then((items) => {
      setWarehouses(items);
      const defaultWarehouse = items.find((warehouse) => warehouse.code === 'DEFAULT') ?? items[0];
      if (defaultWarehouse) setWorkWarehouseId(String(defaultWarehouse.warehouseId));
    }).catch(() => setWarehouses([]));
  }, []);

  const handleInbound = async (productId: number) => {
    if (!workWarehouseId) { alert('작업할 창고를 먼저 선택하세요.'); return; }
    const qtyStr = prompt('입고 수량을 입력하세요:');
    if (!qtyStr) return;
    const qty = parseInt(qtyStr, 10);
    if (isNaN(qty) || qty <= 0) { alert('올바른 수량을 입력하세요.'); return; }
    try {
      await adminService.inbound(Number(workWarehouseId), productId, qty);
      setReloadKey((prev) => prev + 1);
      alert('입고 처리가 완료되었습니다.');
    } catch (err) {
      alert(err instanceof Error ? err.message : '입고 처리에 실패했습니다.');
    }
  };

  const handleAdjust = async (productId: number) => {
    if (!workWarehouseId) { alert('작업할 창고를 먼저 선택하세요.'); return; }
    const qtyStr = prompt('선택한 창고의 조정 후 실재고 수량을 입력하세요:');
    if (!qtyStr) return;
    const qty = parseInt(qtyStr, 10);
    if (isNaN(qty) || qty < 0) { alert('올바른 수량을 입력하세요.'); return; }
    try {
      await adminService.adjust(Number(workWarehouseId), productId, qty);
      setReloadKey((prev) => prev + 1);
      alert('재고 조정이 완료되었습니다.');
    } catch (err) {
      alert(err instanceof Error ? err.message : '재고 조정에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="재고 관리">
      <div className="grid grid-cols-3 gap-4 mb-6">
        <StatCard
          title="정상 재고"
          value={`${stockCounts.normal}개`}
          iconBgColor="bg-green-100"
          icon={<svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>}
        />
        <StatCard
          title="재고 부족"
          value={`${stockCounts.low}개`}
          iconBgColor="bg-yellow-100"
          icon={<svg className="w-5 h-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>}
        />
        <StatCard
          title="품절"
          value={`${stockCounts.out}개`}
          iconBgColor="bg-red-100"
          icon={<svg className="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M6 18L18 6M6 6l12 12" /></svg>}
        />
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex flex-wrap gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') { setSearchKeyword(keyword); setPage(1); }
          }}
          placeholder="상품명 검색"
          className="flex-1 min-w-[200px] border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(1); }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none bg-white"
        >
          <option value="ALL">전체 상태</option>
          <option value="NORMAL">정상</option>
          <option value="LOW_STOCK">부족</option>
          <option value="OUT_OF_STOCK">품절</option>
        </select>
        <select
          value={workWarehouseId}
          onChange={(e) => setWorkWarehouseId(e.target.value)}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none bg-white"
          aria-label="입고 및 조정 작업 창고"
        >
          <option value="">작업 창고 선택</option>
          {warehouses.map((warehouse) => (
            <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>
          ))}
        </select>
        <Button variant="primary" size="sm" onClick={() => { setSearchKeyword(keyword); setPage(1); }}>
          검색
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            downloadCsv(
              `inventory_${new Date().toISOString().slice(0,10)}.csv`,
              ['상품명', '재고', '상태', '최저 재고 임계'],
              inventory.map((i) => [i.productName, i.stockQuantity, INVENTORY_STATUS_LABEL[i.status] ?? i.status, i.lowStockThreshold ?? ''])
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
        <DataTable<ApiInventoryItem>
          keyField="productId"
          data={inventory}
          emptyMessage="재고 데이터가 없습니다."
          columns={[
            { key: 'productId', header: 'ID' },
            { key: 'productName', header: '상품명' },
            {
              key: 'stockQuantity',
              header: '현재 재고',
              render: (row) => (
                <span
                  className={[
                    'font-semibold',
                    row.stockQuantity === 0
                      ? 'text-red-500'
                      : row.stockQuantity <= row.lowStockThreshold
                      ? 'text-yellow-600'
                      : 'text-green-600',
                  ].join(' ')}
                >
                  {row.stockQuantity}개
                </span>
              ),
            },
            { key: 'lowStockThreshold', header: '기준재고', render: (row) => `${row.lowStockThreshold}개` },
            {
              key: 'status',
              header: '상태',
              render: (row) => (
                <span className={`text-xs font-medium px-2 py-0.5 ${INVENTORY_STATUS_COLOR[row.status] ?? ''}`}>
                  {INVENTORY_STATUS_LABEL[row.status] ?? row.status}
                </span>
              ),
            },
            {
              key: 'actions',
              header: '관리',
              render: (row) => (
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" onClick={() => handleInbound(row.productId)}>입고</Button>
                  <Button variant="ghost" size="sm" onClick={() => handleAdjust(row.productId)}>조정</Button>
                </div>
              ),
            },
          ]}
        />
      )}

      {!loading && !error && (
        <div className="mt-2 text-xs text-[#aaa]">총 {totalElements}개 상품</div>
      )}

      {!loading && !error && (
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      )}
    </AdminLayout>
  );
}
