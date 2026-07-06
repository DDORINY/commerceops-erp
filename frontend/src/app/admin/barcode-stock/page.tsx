'use client';

import { useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import DataTable from '@/components/admin/DataTable';
import { getUserRole } from '@/lib/auth';
import { barcodeStockService, type ApiBarcodeStock, type ApiBarcodeStockChange, type ApiBarcodeWarehouseStock } from '@/lib/services/barcodeStockService';
import { permissionGroupService } from '@/lib/services/permissionGroupService';
import { warehouseService, type ApiWarehouse } from '@/lib/services/warehouseService';

export default function AdminBarcodeStockPage() {
  const [barcode, setBarcode] = useState('');
  const [stock, setStock] = useState<ApiBarcodeStock | null>(null);
  const [warehouses, setWarehouses] = useState<ApiWarehouse[]>([]);
  const [warehouseId, setWarehouseId] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [memo, setMemo] = useState('');
  const [loading, setLoading] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [lastChange, setLastChange] = useState<ApiBarcodeStockChange | null>(null);
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);

  const role = getUserRole();
  const canWriteInventory = role === 'SUPER_ADMIN' || permissionCodes.includes('INVENTORY_WRITE') || (!permissionCodes.length && role === 'ADMIN');

  useEffect(() => {
    permissionGroupService.getMyEffectivePermissions()
      .then((response) => setPermissionCodes(response.permissionCodes))
      .catch(() => setPermissionCodes([]));

    warehouseService.getWarehouses()
      .then((items) => {
        setWarehouses(items.filter((warehouse) => warehouse.active));
        const first = items.find((warehouse) => warehouse.active);
        if (first) setWarehouseId(String(first.warehouseId));
      })
      .catch(() => setWarehouses([]));
  }, []);

  const loadStock = async () => {
    const normalized = barcode.trim();
    if (!normalized) {
      setError('바코드를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');
    setLastChange(null);
    try {
      const result = await barcodeStockService.getStock(normalized);
      setStock(result);
      setMessage('바코드 재고를 조회했습니다.');
    } catch (err) {
      setStock(null);
      setError(err instanceof Error ? err.message : '바코드 재고 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const processStock = async (type: 'INBOUND' | 'OUTBOUND') => {
    const normalized = barcode.trim();
    const selectedWarehouseId = Number(warehouseId);
    if (!normalized || !selectedWarehouseId || quantity < 1) {
      setError('바코드, 창고, 수량을 확인해주세요.');
      return;
    }
    if (!canWriteInventory) {
      setError('재고 입출고를 처리할 권한이 없습니다.');
      return;
    }

    setProcessing(true);
    setError('');
    setMessage('');
    try {
      const result = type === 'INBOUND'
        ? await barcodeStockService.inbound(normalized, selectedWarehouseId, quantity, memo || undefined)
        : await barcodeStockService.outbound(normalized, selectedWarehouseId, quantity, memo || undefined);
      setLastChange(result);
      setMessage(type === 'INBOUND' ? '바코드 입고를 처리했습니다.' : '바코드 출고를 처리했습니다.');
      setStock(await barcodeStockService.getStock(normalized));
    } catch (err) {
      setError(err instanceof Error ? err.message : '바코드 재고 처리에 실패했습니다.');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <AdminLayout title="바코드 입출고">
      <div className="mb-5">
        <h1 className="text-xl font-semibold text-[#1a1f2e]">바코드 입출고</h1>
        <p className="mt-1 text-sm text-[#6f7a8a]">SKU 바코드를 기준으로 현재 재고를 조회하고 간단 입고/출고를 처리합니다.</p>
      </div>

      <div className="mb-4 border border-[#e8eaf0] bg-white p-4">
        <div className="flex flex-wrap gap-3">
          <input
            value={barcode}
            onChange={(event) => setBarcode(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') loadStock();
            }}
            placeholder="바코드 입력 또는 스캔"
            className="min-w-[240px] flex-1 border border-[#dfe3ea] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
          />
          <Button variant="outline" size="sm" onClick={loadStock} disabled={loading}>
            {loading ? '조회 중' : '재고 조회'}
          </Button>
        </div>
      </div>

      {message && <div className="mb-3 border border-[#b7dfc1] bg-[#f0fff4] px-4 py-3 text-sm text-[#246b38]">{message}</div>}
      {error && <div className="mb-3 border border-[#f1c7c7] bg-[#fff6f6] px-4 py-3 text-sm text-[#c43a3a]">{error}</div>}

      {stock ? (
        <>
          <div className="mb-4 border border-[#e8eaf0] bg-white p-4">
            <div className="grid gap-3 md:grid-cols-4">
              <div>
                <p className="text-xs text-[#8a9bb5]">상품</p>
                <p className="mt-1 font-semibold text-[#1a1f2e]">{stock.productName}</p>
              </div>
              <div>
                <p className="text-xs text-[#8a9bb5]">SKU</p>
                <p className="mt-1 font-semibold text-[#1a1f2e]">{stock.skuCode}</p>
              </div>
              <div>
                <p className="text-xs text-[#8a9bb5]">바코드</p>
                <p className="mt-1 font-semibold text-[#1a1f2e]">{stock.barcode}</p>
              </div>
              <div>
                <p className="text-xs text-[#8a9bb5]">상품 총재고</p>
                <p className="mt-1 font-semibold text-[#1a1f2e]">{stock.productStockQuantity.toLocaleString()}</p>
              </div>
            </div>
          </div>

          <div className="mb-4 border border-[#e8eaf0] bg-white p-4">
            <h2 className="mb-3 text-base font-semibold text-[#1a1f2e]">입출고 처리</h2>
            <div className="grid gap-3 md:grid-cols-[1fr_140px_1fr_auto_auto]">
              <select
                value={warehouseId}
                onChange={(event) => setWarehouseId(event.target.value)}
                className="border border-[#dfe3ea] px-3 py-2 text-sm outline-none"
              >
                {warehouses.map((warehouse) => (
                  <option key={warehouse.warehouseId} value={warehouse.warehouseId}>{warehouse.name}</option>
                ))}
              </select>
              <input
                type="number"
                min={1}
                value={quantity}
                onChange={(event) => setQuantity(Math.max(1, Number(event.target.value)))}
                className="border border-[#dfe3ea] px-3 py-2 text-sm outline-none"
              />
              <input
                value={memo}
                onChange={(event) => setMemo(event.target.value)}
                placeholder="메모"
                className="border border-[#dfe3ea] px-3 py-2 text-sm outline-none"
              />
              <Button variant="primary" size="sm" onClick={() => processStock('INBOUND')} disabled={processing || !canWriteInventory}>입고</Button>
              <Button variant="outline" size="sm" onClick={() => processStock('OUTBOUND')} disabled={processing || !canWriteInventory}>출고</Button>
            </div>
            {!canWriteInventory && <p className="mt-2 text-xs text-[#c43a3a]">현재 계정은 재고 입출고 권한이 없어 조회만 가능합니다.</p>}
          </div>

          {lastChange && (
            <div className="mb-4 border border-[#dfe3ea] bg-[#fafbfc] p-4 text-sm text-[#566171]">
              <p className="font-semibold text-[#1a1f2e]">최근 처리 결과</p>
              <p className="mt-1">{lastChange.type === 'INBOUND' ? '입고' : '출고'} {lastChange.quantity.toLocaleString()}개 · 상품 재고 {lastChange.beforeProductStock.toLocaleString()} → {lastChange.afterProductStock.toLocaleString()} · 창고 재고 {lastChange.beforeWarehouseStock.toLocaleString()} → {lastChange.afterWarehouseStock.toLocaleString()}</p>
            </div>
          )}

          <DataTable<ApiBarcodeWarehouseStock>
            keyField="warehouseId"
            data={stock.warehouseStocks}
            emptyMessage="창고별 재고가 없습니다."
            columns={[
              { key: 'warehouseName', header: '창고' },
              { key: 'warehouseCode', header: '창고 코드' },
              { key: 'quantity', header: '수량', render: (row) => row.quantity.toLocaleString() },
              { key: 'reservedQuantity', header: '예약', render: (row) => row.reservedQuantity.toLocaleString() },
              { key: 'availableQuantity', header: '가용', render: (row) => row.availableQuantity.toLocaleString() },
            ]}
          />
        </>
      ) : (
        <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#999]">바코드를 조회하면 SKU와 창고별 재고가 표시됩니다.</div>
      )}
    </AdminLayout>
  );
}
