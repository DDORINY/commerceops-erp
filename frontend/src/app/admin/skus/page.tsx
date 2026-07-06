'use client';

import { FormEvent, useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { getUserRole } from '@/lib/auth';
import { permissionGroupService } from '@/lib/services/permissionGroupService';
import { skuService, type ApiSku, type SkuSaveRequest } from '@/lib/services/skuService';

const PAGE_SIZE = 10;

type ActiveFilter = 'ALL' | 'true' | 'false';
type BarcodeFilter = 'ALL' | 'true' | 'false';

type FormState = {
  productId: string;
  optionSignature: string;
  skuCode: string;
  barcode: string;
  name: string;
  safetyStockQuantity: string;
  active: boolean;
};

const emptyForm: FormState = {
  productId: '',
  optionSignature: '',
  skuCode: '',
  barcode: '',
  name: '',
  safetyStockQuantity: '0',
  active: true,
};

export default function AdminSkusPage() {
  const [items, setItems] = useState<ApiSku[]>([]);
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [productIdFilter, setProductIdFilter] = useState('');
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('ALL');
  const [barcodeFilter, setBarcodeFilter] = useState<BarcodeFilter>('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [editingSku, setEditingSku] = useState<ApiSku | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);

  const role = getUserRole();
  const hasSkuManage = role === 'SUPER_ADMIN' || permissionCodes.includes('SKU_MANAGE') || (!permissionCodes.length && role === 'ADMIN');
  const hasBarcodeManage = role === 'SUPER_ADMIN' || permissionCodes.includes('BARCODE_MANAGE') || (!permissionCodes.length && role === 'ADMIN');

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
        const response = await skuService.getSkus({
          keyword: searchKeyword || undefined,
          productId: productIdFilter ? Number(productIdFilter) : undefined,
          active: activeFilter === 'ALL' ? 'ALL' : activeFilter === 'true',
          hasBarcode: barcodeFilter === 'ALL' ? 'ALL' : barcodeFilter === 'true',
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
        setError(err instanceof Error ? err.message : 'SKU 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    load();
    return () => {
      mounted = false;
    };
  }, [searchKeyword, productIdFilter, activeFilter, barcodeFilter, page, reloadKey]);

  const openCreateForm = () => {
    setEditingSku(null);
    setForm(emptyForm);
    setMessage('');
    setError('');
    setFormOpen(true);
  };

  const openEditForm = (sku: ApiSku) => {
    setEditingSku(sku);
    setForm({
      productId: String(sku.productId),
      optionSignature: sku.optionSignature ?? '',
      skuCode: sku.skuCode,
      barcode: sku.barcode ?? '',
      name: sku.name,
      safetyStockQuantity: String(sku.safetyStockQuantity ?? 0),
      active: sku.active,
    });
    setMessage('');
    setError('');
    setFormOpen(true);
  };

  const closeForm = () => {
    if (saving) return;
    setFormOpen(false);
    setEditingSku(null);
    setForm(emptyForm);
  };

  const handleSearch = () => {
    setSearchKeyword(keyword);
    setPage(1);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!hasSkuManage) {
      setError('SKU를 저장할 권한이 없습니다.');
      return;
    }
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const safetyStockQuantity = form.safetyStockQuantity ? Number(form.safetyStockQuantity) : 0;
      const payload: SkuSaveRequest = {
        optionSignature: normalize(form.optionSignature),
        skuCode: normalize(form.skuCode),
        barcode: normalize(form.barcode),
        name: normalize(form.name),
        safetyStockQuantity,
      };
      if (!editingSku) {
        payload.productId = Number(form.productId);
        payload.active = form.active;
      }
      if (!editingSku && (!payload.productId || Number.isNaN(payload.productId))) {
        setError('상품 ID를 입력해주세요.');
        return;
      }
      if (Number.isNaN(safetyStockQuantity) || safetyStockQuantity < 0) {
        setError('안전 재고는 0 이상의 숫자로 입력해주세요.');
        return;
      }

      if (editingSku) {
        await skuService.updateSku(editingSku.id, payload);
        setMessage('SKU가 수정되었습니다.');
      } else {
        await skuService.createSku(payload);
        setMessage('SKU가 생성되었습니다.');
      }
      setFormOpen(false);
      setEditingSku(null);
      setForm(emptyForm);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'SKU 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleActiveToggle = async (sku: ApiSku) => {
    if (!hasSkuManage) {
      setError('SKU 활성 상태를 변경할 권한이 없습니다.');
      return;
    }
    try {
      await skuService.updateActive(sku.id, !sku.active);
      setMessage('SKU 활성 상태가 변경되었습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'SKU 활성 상태 변경에 실패했습니다.');
    }
  };

  const handleRegenerateBarcode = async (sku: ApiSku) => {
    if (!hasBarcodeManage) {
      setError('바코드를 재발급할 권한이 없습니다.');
      return;
    }
    if (!confirm(`${sku.skuCode} 바코드를 재발급할까요?`)) return;
    try {
      await skuService.regenerateBarcode(sku.id);
      setMessage('바코드가 재발급되었습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '바코드 재발급에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="SKU/바코드 관리">
      <div className="flex items-center justify-between gap-3 mb-5">
        <div>
          <h1 className="text-xl font-semibold text-[#1a1f2e]">SKU/바코드 관리</h1>
          <p className="mt-1 text-sm text-[#6f7a8a]">
            상품 마스터와 분리된 입출고, 재고, 바코드 기준 코드를 관리합니다.
          </p>
        </div>
        <Button variant="primary" size="sm" onClick={openCreateForm} disabled={!hasSkuManage}>
          SKU 생성
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
          placeholder="SKU, 바코드, 상품명 검색"
          className="flex-1 min-w-[220px] border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <input
          type="number"
          value={productIdFilter}
          onChange={(event) => {
            setProductIdFilter(event.target.value);
            setPage(1);
          }}
          placeholder="상품 ID"
          className="w-[120px] border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <select
          value={activeFilter}
          onChange={(event) => {
            setActiveFilter(event.target.value as ActiveFilter);
            setPage(1);
          }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none bg-white"
        >
          <option value="ALL">전체 상태</option>
          <option value="true">활성</option>
          <option value="false">비활성</option>
        </select>
        <select
          value={barcodeFilter}
          onChange={(event) => {
            setBarcodeFilter(event.target.value as BarcodeFilter);
            setPage(1);
          }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none bg-white"
        >
          <option value="ALL">바코드 전체</option>
          <option value="true">바코드 있음</option>
          <option value="false">바코드 없음</option>
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
        <div className="py-12 text-center text-[#999] text-sm bg-white border border-[#e8eaf0]">SKU 목록을 불러오는 중입니다.</div>
      ) : (
        <>
          <DataTable<ApiSku>
            keyField="id"
            data={items}
            emptyMessage="등록된 SKU가 없습니다."
            columns={[
              { key: 'id', header: 'ID' },
              {
                key: 'productName',
                header: '상품',
                render: (row) => (
                  <div>
                    <p className="font-medium text-[#1a1f2e]">{row.productName}</p>
                    <p className="text-xs text-[#8a9bb5]">{row.productCode || `상품 ID ${row.productId}`}</p>
                  </div>
                ),
              },
              { key: 'skuCode', header: 'SKU 코드' },
              { key: 'barcode', header: '바코드', render: (row) => row.barcode || '-' },
              { key: 'optionSignature', header: '옵션', render: (row) => row.optionSignature || '-' },
              { key: 'safetyStockQuantity', header: '안전 재고', render: (row) => `${row.safetyStockQuantity}개` },
              {
                key: 'active',
                header: '상태',
                render: (row) => (
                  <span className={row.active ? 'text-xs font-medium text-[#267a3d]' : 'text-xs font-medium text-[#a04b4b]'}>
                    {row.active ? '활성' : '비활성'}
                  </span>
                ),
              },
              {
                key: 'actions',
                header: '관리',
                render: (row) => (
                  <div className="flex flex-wrap gap-2">
                    <Button variant="outline" size="sm" onClick={() => openEditForm(row)} disabled={!hasSkuManage}>
                      수정
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handleActiveToggle(row)} disabled={!hasSkuManage}>
                      {row.active ? '비활성' : '활성'}
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handleRegenerateBarcode(row)} disabled={!hasBarcodeManage}>
                      바코드 재발급
                    </Button>
                  </div>
                ),
              },
            ]}
          />
          <div className="mt-2 text-xs text-[#8a9bb5]">총 {totalElements}개 SKU</div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      {formOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4">
          <div className="w-full max-w-[620px] bg-white border border-[#dfe3ea] shadow-xl">
            <div className="flex items-center justify-between border-b border-[#edf0f5] px-5 py-4">
              <h2 className="text-base font-semibold text-[#1a1f2e]">{editingSku ? 'SKU 수정' : 'SKU 생성'}</h2>
              <button type="button" onClick={closeForm} className="text-sm text-[#6f7a8a] hover:text-[#1a1f2e]">
                닫기
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-5 grid grid-cols-2 gap-4">
              <label className="col-span-2 text-sm">
                <span className="block mb-1 text-[#566171]">상품 ID</span>
                <input
                  type="number"
                  value={form.productId}
                  onChange={(event) => setForm((prev) => ({ ...prev, productId: event.target.value }))}
                  disabled={Boolean(editingSku)}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e] disabled:bg-[#f7f8fa]"
                  placeholder="상품 ID를 입력하세요"
                />
              </label>
              <label className="text-sm">
                <span className="block mb-1 text-[#566171]">SKU 코드</span>
                <input
                  type="text"
                  value={form.skuCode}
                  onChange={(event) => setForm((prev) => ({ ...prev, skuCode: event.target.value }))}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]"
                  placeholder="비우면 자동 생성"
                />
              </label>
              <label className="text-sm">
                <span className="block mb-1 text-[#566171]">바코드</span>
                <input
                  type="text"
                  value={form.barcode}
                  onChange={(event) => setForm((prev) => ({ ...prev, barcode: event.target.value }))}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]"
                  placeholder="비우면 자동 생성"
                />
              </label>
              <label className="col-span-2 text-sm">
                <span className="block mb-1 text-[#566171]">SKU명</span>
                <input
                  type="text"
                  value={form.name}
                  onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]"
                  placeholder="비우면 상품명 기준으로 생성"
                />
              </label>
              <label className="col-span-2 text-sm">
                <span className="block mb-1 text-[#566171]">옵션 서명</span>
                <input
                  type="text"
                  value={form.optionSignature}
                  onChange={(event) => setForm((prev) => ({ ...prev, optionSignature: event.target.value }))}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]"
                  placeholder="예: color=black;size=M"
                />
              </label>
              <label className="text-sm">
                <span className="block mb-1 text-[#566171]">안전 재고</span>
                <input
                  type="number"
                  min={0}
                  value={form.safetyStockQuantity}
                  onChange={(event) => setForm((prev) => ({ ...prev, safetyStockQuantity: event.target.value }))}
                  className="w-full border border-[#e0e0e0] px-3 py-2 outline-none focus:border-[#1a1f2e]"
                />
              </label>
              <label className="flex items-end gap-2 text-sm pb-2">
                <input
                  type="checkbox"
                  checked={form.active}
                  onChange={(event) => setForm((prev) => ({ ...prev, active: event.target.checked }))}
                  disabled={Boolean(editingSku)}
                />
                활성 상태로 생성
              </label>
              <div className="col-span-2 flex justify-end gap-2 pt-2">
                <Button type="button" variant="ghost" size="sm" onClick={closeForm} disabled={saving}>
                  취소
                </Button>
                <Button type="submit" variant="primary" size="sm" disabled={saving || !hasSkuManage}>
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
