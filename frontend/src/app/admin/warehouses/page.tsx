'use client';

import { FormEvent, useCallback, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import Pagination from '@/components/common/Pagination';
import { productService, type ApiProductItem } from '@/lib/services/productService';
import {
  warehouseService,
  type ApiStockTransfer,
  type ApiWarehouse,
  type ApiWarehouseStock,
} from '@/lib/services/warehouseService';
import { formatDateTime } from '@/lib/format';

type Tab = 'warehouses' | 'stocks' | 'transfers';
const PAGE_SIZE = 15;

export default function AdminWarehousesPage() {
  const [tab, setTab] = useState<Tab>('warehouses');
  const [warehouses, setWarehouses] = useState<ApiWarehouse[]>([]);
  const [products, setProducts] = useState<ApiProductItem[]>([]);
  const [stocks, setStocks] = useState<ApiWarehouseStock[]>([]);
  const [transfers, setTransfers] = useState<ApiStockTransfer[]>([]);
  const [stockPage, setStockPage] = useState(1);
  const [stockPages, setStockPages] = useState(1);
  const [transferPage, setTransferPage] = useState(1);
  const [transferPages, setTransferPages] = useState(1);
  const [stockWarehouseFilter, setStockWarehouseFilter] = useState('');
  const [transferStatus, setTransferStatus] = useState('ALL');
  const [loadingStocks, setLoadingStocks] = useState(true);
  const [loadingTransfers, setLoadingTransfers] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const [warehouseForm, setWarehouseForm] = useState({ code: '', name: '', address: '' });
  const [allocationForm, setAllocationForm] = useState({ warehouseId: '', productId: '', quantity: '' });
  const [transferForm, setTransferForm] = useState({
    fromWarehouseId: '', toWarehouseId: '', productId: '', quantity: '',
  });

  const fetchWarehouses = useCallback(() => {
    warehouseService.getWarehouses().then(setWarehouses).catch(() => setWarehouses([]));
  }, []);

  const fetchStocks = useCallback(() => {
    warehouseService
      .getStocks(stockWarehouseFilter ? Number(stockWarehouseFilter) : undefined, undefined, stockPage - 1, PAGE_SIZE)
      .then((response) => {
        setStocks(response.content);
        setStockPages(response.totalPages || 1);
      })
      .catch(() => setStocks([]))
      .finally(() => setLoadingStocks(false));
  }, [stockWarehouseFilter, stockPage]);

  const fetchTransfers = useCallback(() => {
    warehouseService
      .getTransfers(transferStatus, transferPage - 1, PAGE_SIZE)
      .then((response) => {
        setTransfers(response.content);
        setTransferPages(response.totalPages || 1);
      })
      .catch(() => setTransfers([]))
      .finally(() => setLoadingTransfers(false));
  }, [transferStatus, transferPage]);

  useEffect(() => {
    fetchWarehouses();
    productService.getProducts({ size: 100 }).then((response) => setProducts(response.content)).catch(() => {});
  }, [fetchWarehouses]);

  useEffect(() => {
    fetchStocks();
  }, [fetchStocks]);

  useEffect(() => {
    fetchTransfers();
  }, [fetchTransfers]);

  const createWarehouse = async (event: FormEvent) => {
    event.preventDefault();
    if (!warehouseForm.code.trim() || !warehouseForm.name.trim() || !warehouseForm.address.trim()) {
      alert('창고 코드, 이름, 주소를 모두 입력하세요.');
      return;
    }
    try {
      setSubmitting(true);
      await warehouseService.createWarehouse(
        warehouseForm.code.trim(), warehouseForm.name.trim(), warehouseForm.address.trim()
      );
      setWarehouseForm({ code: '', name: '', address: '' });
      fetchWarehouses();
    } catch (error) {
      alert(error instanceof Error ? error.message : '창고 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const allocateStock = async (event: FormEvent) => {
    event.preventDefault();
    const warehouseId = Number(allocationForm.warehouseId);
    const productId = Number(allocationForm.productId);
    const quantity = Number(allocationForm.quantity);
    if (!warehouseId || !productId || quantity < 1) {
      alert('창고, 상품과 1개 이상의 수량을 입력하세요.');
      return;
    }
    try {
      setSubmitting(true);
      await warehouseService.allocateStock(warehouseId, productId, quantity);
      setAllocationForm({ warehouseId: '', productId: '', quantity: '' });
      fetchStocks();
    } catch (error) {
      alert(error instanceof Error ? error.message : '재고 배치에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const createTransfer = async (event: FormEvent) => {
    event.preventDefault();
    const fromWarehouseId = Number(transferForm.fromWarehouseId);
    const toWarehouseId = Number(transferForm.toWarehouseId);
    const productId = Number(transferForm.productId);
    const quantity = Number(transferForm.quantity);
    if (!fromWarehouseId || !toWarehouseId || !productId || quantity < 1) {
      alert('출발/도착 창고, 상품과 수량을 입력하세요.');
      return;
    }
    if (fromWarehouseId === toWarehouseId) {
      alert('출발 창고와 도착 창고가 같을 수 없습니다.');
      return;
    }
    try {
      setSubmitting(true);
      await warehouseService.createTransfer(fromWarehouseId, toWarehouseId, productId, quantity);
      setTransferForm({ fromWarehouseId: '', toWarehouseId: '', productId: '', quantity: '' });
      fetchTransfers();
    } catch (error) {
      alert(error instanceof Error ? error.message : '재고 이동 요청에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const completeTransfer = async (transferId: number) => {
    if (!confirm('이 재고 이동을 완료 처리하시겠습니까?')) return;
    try {
      await warehouseService.completeTransfer(transferId);
      fetchTransfers();
      fetchStocks();
    } catch (error) {
      alert(error instanceof Error ? error.message : '재고 이동 완료 처리에 실패했습니다.');
    }
  };

  const selectClass = 'border border-[#dfe3ed] bg-white px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]';
  const inputClass = 'border border-[#dfe3ed] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]';

  return (
    <AdminLayout title="창고 관리">
      <div className="flex border-b border-[#e8eaf0] mb-6">
        {([
          ['warehouses', '창고'], ['stocks', '창고별 재고'], ['transfers', '재고 이동'],
        ] as const).map(([value, label]) => (
          <button
            key={value}
            onClick={() => setTab(value)}
            className={`px-5 py-3 text-sm font-medium ${
              tab === value ? 'border-b-2 border-[#1a1f2e] text-[#1a1f2e]' : 'text-[#8a9bb5]'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === 'warehouses' && (
        <div className="space-y-6">
          <form onSubmit={createWarehouse} className="bg-white border border-[#e8eaf0] p-5">
            <h2 className="font-semibold text-sm mb-4">신규 창고 등록</h2>
            <div className="grid grid-cols-1 md:grid-cols-[160px_1fr_2fr_auto] gap-3">
              <input className={inputClass} placeholder="코드 (SEOUL_01)" value={warehouseForm.code}
                onChange={(e) => setWarehouseForm({ ...warehouseForm, code: e.target.value })} />
              <input className={inputClass} placeholder="창고 이름" value={warehouseForm.name}
                onChange={(e) => setWarehouseForm({ ...warehouseForm, name: e.target.value })} />
              <input className={inputClass} placeholder="주소" value={warehouseForm.address}
                onChange={(e) => setWarehouseForm({ ...warehouseForm, address: e.target.value })} />
              <Button type="submit" disabled={submitting}>등록</Button>
            </div>
          </form>

          <DataTable<ApiWarehouse>
            keyField="warehouseId"
            data={warehouses}
            emptyMessage="등록된 창고가 없습니다."
            columns={[
              { key: 'code', header: '코드', render: (row) => <span className="font-mono text-xs">{row.code}</span> },
              { key: 'name', header: '창고명' },
              { key: 'address', header: '주소' },
              { key: 'active', header: '상태', render: (row) => (
                <span className={row.active ? 'text-green-600' : 'text-[#aaa]'}>{row.active ? '운영 중' : '비활성'}</span>
              ) },
              { key: 'createdAt', header: '등록일', render: (row) => formatDateTime(row.createdAt) },
            ]}
          />
        </div>
      )}

      {tab === 'stocks' && (
        <div className="space-y-5">
          <form onSubmit={allocateStock} className="bg-white border border-[#e8eaf0] p-5">
            <h2 className="font-semibold text-sm mb-1">미배정 재고 배치</h2>
            <p className="text-xs text-[#8a9bb5] mb-4">상품 총재고 중 아직 창고에 배정되지 않은 수량만 배치할 수 있습니다.</p>
            <div className="grid grid-cols-1 md:grid-cols-[1fr_1.5fr_120px_auto] gap-3">
              <select className={selectClass} value={allocationForm.warehouseId}
                onChange={(e) => setAllocationForm({ ...allocationForm, warehouseId: e.target.value })}>
                <option value="">창고 선택</option>
                {warehouses.map((warehouse) => <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>)}
              </select>
              <select className={selectClass} value={allocationForm.productId}
                onChange={(e) => setAllocationForm({ ...allocationForm, productId: e.target.value })}>
                <option value="">상품 선택</option>
                {products.map((product) => <option key={product.id} value={product.id}>{product.name} (총 {product.stockQuantity})</option>)}
              </select>
              <input className={inputClass} type="number" min="1" placeholder="수량" value={allocationForm.quantity}
                onChange={(e) => setAllocationForm({ ...allocationForm, quantity: e.target.value })} />
              <Button type="submit" disabled={submitting}>배치</Button>
            </div>
          </form>

          <div className="flex justify-end">
            <select className={selectClass} value={stockWarehouseFilter}
              onChange={(e) => { setStockWarehouseFilter(e.target.value); setStockPage(1); }}>
              <option value="">전체 창고</option>
              {warehouses.map((warehouse) => <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>)}
            </select>
          </div>

          {loadingStocks ? <div className="py-12 text-center text-[#aaa]">로딩 중...</div> : (
            <DataTable<ApiWarehouseStock>
              keyField="stockId"
              data={stocks}
              emptyMessage="창고에 배정된 재고가 없습니다."
              columns={[
                { key: 'warehouseName', header: '창고', render: (row) => <><b>{row.warehouseName}</b><span className="ml-2 text-xs text-[#aaa]">{row.warehouseCode}</span></> },
                { key: 'productName', header: '상품' },
                { key: 'quantity', header: '실재고', render: (row) => <span className="font-semibold tabular-nums">{row.quantity}개</span> },
                { key: 'reservedQuantity', header: '예약', render: (row) => <span className="text-amber-600">{row.reservedQuantity}개</span> },
                { key: 'availableQuantity', header: '가용', render: (row) => <span className="text-green-600 font-semibold">{row.availableQuantity}개</span> },
                { key: 'totalProductStock', header: '상품 가용 총재고', render: (row) => `${row.totalProductStock}개` },
              ]}
            />
          )}
          <Pagination currentPage={stockPage} totalPages={stockPages} onPageChange={setStockPage} />
        </div>
      )}

      {tab === 'transfers' && (
        <div className="space-y-5">
          <form onSubmit={createTransfer} className="bg-white border border-[#e8eaf0] p-5">
            <h2 className="font-semibold text-sm mb-4">재고 이동 요청</h2>
            <div className="grid grid-cols-1 lg:grid-cols-[1fr_1fr_1.5fr_100px_auto] gap-3">
              <select className={selectClass} value={transferForm.fromWarehouseId}
                onChange={(e) => setTransferForm({ ...transferForm, fromWarehouseId: e.target.value })}>
                <option value="">출발 창고</option>
                {warehouses.map((warehouse) => <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>)}
              </select>
              <select className={selectClass} value={transferForm.toWarehouseId}
                onChange={(e) => setTransferForm({ ...transferForm, toWarehouseId: e.target.value })}>
                <option value="">도착 창고</option>
                {warehouses.map((warehouse) => <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>)}
              </select>
              <select className={selectClass} value={transferForm.productId}
                onChange={(e) => setTransferForm({ ...transferForm, productId: e.target.value })}>
                <option value="">상품 선택</option>
                {products.map((product) => <option key={product.id} value={product.id}>{product.name}</option>)}
              </select>
              <input className={inputClass} type="number" min="1" placeholder="수량" value={transferForm.quantity}
                onChange={(e) => setTransferForm({ ...transferForm, quantity: e.target.value })} />
              <Button type="submit" disabled={submitting}>이동 요청</Button>
            </div>
          </form>

          <div className="flex justify-end">
            <select className={selectClass} value={transferStatus}
              onChange={(e) => { setTransferStatus(e.target.value); setTransferPage(1); }}>
              <option value="ALL">전체 상태</option>
              <option value="PENDING">이동 대기</option>
              <option value="COMPLETED">이동 완료</option>
            </select>
          </div>

          {loadingTransfers ? <div className="py-12 text-center text-[#aaa]">로딩 중...</div> : (
            <DataTable<ApiStockTransfer>
              keyField="transferId"
              data={transfers}
              emptyMessage="재고 이동 내역이 없습니다."
              columns={[
                { key: 'transferNumber', header: '이동번호', render: (row) => <span className="font-mono text-xs">{row.transferNumber}</span> },
                { key: 'route', header: '이동 경로', render: (row) => <span>{row.fromWarehouseName} <b className="mx-2">→</b> {row.toWarehouseName}</span> },
                { key: 'productName', header: '상품' },
                { key: 'quantity', header: '수량', render: (row) => `${row.quantity}개` },
                { key: 'status', header: '상태', render: (row) => (
                  <span className={`px-2 py-1 text-xs ${row.status === 'COMPLETED' ? 'bg-green-50 text-green-700' : 'bg-amber-50 text-amber-700'}`}>
                    {row.status === 'COMPLETED' ? '이동 완료' : '이동 대기'}
                  </span>
                ) },
                { key: 'requestedAt', header: '요청일', render: (row) => formatDateTime(row.requestedAt) },
                { key: 'action', header: '처리', render: (row) => row.status === 'PENDING' ? (
                  <Button size="sm" onClick={() => completeTransfer(row.transferId)}>완료 처리</Button>
                ) : <span className="text-xs text-[#aaa]">완료</span> },
              ]}
            />
          )}
          <Pagination currentPage={transferPage} totalPages={transferPages} onPageChange={setTransferPage} />
        </div>
      )}
    </AdminLayout>
  );
}
