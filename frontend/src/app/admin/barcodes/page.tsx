'use client';

import { useEffect, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import { getUserRole } from '@/lib/auth';
import { barcodeService, type ApiBarcodeLabel, type ApiBarcodeLabelPreview, type ApiBarcodeSku } from '@/lib/services/barcodeService';
import { permissionGroupService } from '@/lib/services/permissionGroupService';

const PAGE_SIZE = 10;

export default function AdminBarcodesPage() {
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [skus, setSkus] = useState<ApiBarcodeSku[]>([]);
  const [labels, setLabels] = useState<ApiBarcodeLabel[]>([]);
  const [preview, setPreview] = useState<ApiBarcodeLabelPreview | null>(null);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [permissionCodes, setPermissionCodes] = useState<string[]>([]);

  const role = getUserRole();
  const canManageBarcode = role === 'SUPER_ADMIN' || permissionCodes.includes('BARCODE_MANAGE') || (!permissionCodes.length && role === 'ADMIN');

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
        const [skuPage, labelPage] = await Promise.all([
          barcodeService.searchSkus(searchKeyword || undefined, page - 1, PAGE_SIZE),
          barcodeService.getLabels(searchKeyword || undefined, 0, 5),
        ]);

        if (!mounted) return;
        setSkus(skuPage.content);
        setTotalPages(skuPage.totalPages || 1);
        setLabels(labelPage.content);
      } catch (err) {
        if (!mounted) return;
        setSkus([]);
        setLabels([]);
        setTotalPages(1);
        setError(err instanceof Error ? err.message : '바코드 정보를 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, [searchKeyword, page, reloadKey]);

  const handleSearch = () => {
    setSearchKeyword(keyword);
    setPage(1);
  };

  const createLabel = async (sku: ApiBarcodeSku) => {
    setMessage('');
    setError('');

    if (!canManageBarcode) {
      setError('바코드 라벨을 생성할 권한이 없습니다.');
      return;
    }
    if (!sku.barcode) {
      setError('바코드가 없는 SKU는 라벨을 생성할 수 없습니다.');
      return;
    }

    try {
      const result = await barcodeService.createLabel(sku.skuId);
      setPreview(result);
      setMessage('바코드 라벨 미리보기를 생성했습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '바코드 라벨 생성에 실패했습니다.');
    }
  };

  const markPrinted = async () => {
    if (!preview) return;

    setMessage('');
    setError('');

    if (!canManageBarcode) {
      setError('바코드 라벨 출력 이력을 기록할 권한이 없습니다.');
      return;
    }

    try {
      const result = await barcodeService.markPrinted(preview.labelId);
      setPreview(result);
      setMessage('바코드 라벨 출력 이력을 기록했습니다.');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : '바코드 라벨 출력 이력 기록에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="바코드 라벨 관리">
      <div className="mb-5">
        <h1 className="text-xl font-semibold text-[#1a1f2e]">바코드 라벨 관리</h1>
        <p className="mt-1 text-sm text-[#6f7a8a]">SKU 바코드를 조회하고 HTML 라벨 미리보기와 출력 이력을 관리합니다.</p>
      </div>

      <div className="mb-4 flex flex-wrap gap-3 border border-[#e8eaf0] bg-white p-4">
        <input
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') handleSearch();
          }}
          placeholder="SKU, 바코드, 상품명 검색"
          className="min-w-[220px] flex-1 border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <Button variant="outline" size="sm" onClick={handleSearch}>검색</Button>
      </div>

      {message && <div className="mb-3 border border-[#b7dfc1] bg-[#f0fff4] px-4 py-3 text-sm text-[#246b38]">{message}</div>}
      {error && <div className="mb-3 border border-[#f1c7c7] bg-[#fff6f6] px-4 py-3 text-sm text-[#c43a3a]">{error}</div>}

      {loading ? (
        <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#999]">바코드 목록을 불러오는 중입니다.</div>
      ) : (
        <>
          <DataTable<ApiBarcodeSku>
            keyField="skuId"
            data={skus}
            emptyMessage="조회된 SKU가 없습니다."
            columns={[
              { key: 'skuCode', header: 'SKU 코드' },
              { key: 'barcode', header: '바코드', render: (row) => row.barcode || '-' },
              { key: 'productName', header: '상품명' },
              { key: 'skuName', header: 'SKU명' },
              { key: 'stockQuantity', header: '현재 재고', render: (row) => row.stockQuantity.toLocaleString() },
              { key: 'active', header: '상태', render: (row) => (row.active ? '활성' : '비활성') },
              {
                key: 'actions',
                header: '라벨',
                render: (row) => (
                  <Button variant="outline" size="sm" onClick={() => createLabel(row)} disabled={!canManageBarcode || !row.barcode || !row.active}>
                    미리보기 생성
                  </Button>
                ),
              },
            ]}
          />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      {preview && (
        <div className="mt-6 border border-[#dfe3ea] bg-white p-5">
          <div className="flex items-center justify-between gap-3">
            <h2 className="text-base font-semibold text-[#1a1f2e]">라벨 미리보기</h2>
            <Button variant="primary" size="sm" onClick={markPrinted} disabled={!canManageBarcode}>출력 이력 기록</Button>
          </div>
          <div className="mt-4 inline-block min-w-[260px] border border-[#1a1f2e] bg-white p-4 text-center">
            <p className="text-sm font-semibold text-[#1a1f2e]">{preview.productName}</p>
            <p className="mt-2 text-xs text-[#6f7a8a]">SKU: {preview.skuCode}</p>
            <p className="mt-2 text-2xl font-bold tracking-[2px] text-[#111]">{preview.barcode}</p>
            <p className="mt-2 text-xs text-[#6f7a8a]">{preview.skuName}</p>
          </div>
          <p className="mt-3 text-xs text-[#8a9bb5]">실제 프린터 SDK 연동은 제외하고, 현재는 HTML 라벨 미리보기와 출력 이력만 기록합니다.</p>
        </div>
      )}

      <div className="mt-6 border border-[#e8eaf0] bg-white p-4">
        <h2 className="mb-3 text-base font-semibold text-[#1a1f2e]">최근 라벨 이력</h2>
        {labels.length === 0 ? (
          <p className="py-6 text-center text-sm text-[#999]">라벨 이력이 없습니다.</p>
        ) : (
          <div className="space-y-2">
            {labels.map((label) => (
              <div key={label.id} className="flex items-center justify-between border border-[#f0f1f5] px-3 py-2 text-sm">
                <div>
                  <p className="font-medium text-[#1a1f2e]">{label.skuCode} / {label.barcode}</p>
                  <p className="text-xs text-[#8a9bb5]">{label.productName} · {label.labelFormat}</p>
                </div>
                <span className="text-[#566171]">출력 {label.printCount}회</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </AdminLayout>
  );
}
