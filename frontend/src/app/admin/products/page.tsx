'use client';

import { useEffect, useMemo, useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import {
  productService,
  type ApiAdminProductItem,
  type ApiCategory,
  type ProductDisplayStatus,
  type ProductSalesStatus,
} from '@/lib/services/productService';
import {
  formatPrice,
  PRODUCT_DISPLAY_STATUS_LABEL,
  PRODUCT_OPERATION_STATUS_COLOR,
  PRODUCT_SALES_STATUS_LABEL,
} from '@/lib/format';

const PAGE_SIZE = 8;

export default function AdminProductsPage() {
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [salesStatusFilter, setSalesStatusFilter] = useState('ALL');
  const [displayStatusFilter, setDisplayStatusFilter] = useState('ALL');
  const [categoryIdFilter, setCategoryIdFilter] = useState('ALL');
  const [stockStatusFilter, setStockStatusFilter] = useState('ALL');
  const [lowStockOnly, setLowStockOnly] = useState(false);
  const [salePeriodStatusFilter, setSalePeriodStatusFilter] = useState('ALL');
  const [page, setPage] = useState(1);
  const [products, setProducts] = useState<ApiAdminProductItem[]>([]);
  const [categories, setCategories] = useState<ApiCategory[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [bulkSalesStatus, setBulkSalesStatus] = useState('KEEP');
  const [bulkDisplayStatus, setBulkDisplayStatus] = useState('KEEP');
  const [bulkReason, setBulkReason] = useState('');
  const [bulkLoading, setBulkLoading] = useState(false);
  const [bulkMessage, setBulkMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    productService.getCategories()
      .then(setCategories)
      .catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    let mounted = true;

    const loadProducts = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await productService.getAdminProducts({
          status: statusFilter,
          salesStatus: salesStatusFilter,
          displayStatus: displayStatusFilter,
          categoryId: categoryIdFilter !== 'ALL' ? Number(categoryIdFilter) : undefined,
          stockStatus: stockStatusFilter,
          lowStockOnly,
          salePeriodStatus: salePeriodStatusFilter as 'ALL' | 'ACTIVE' | 'UPCOMING' | 'ENDED',
          keyword: searchKeyword || undefined,
          page: page - 1,
          size: PAGE_SIZE,
        });
        if (!mounted) return;
        setProducts(res.content);
        setSelectedIds([]);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages || 1);
      } catch (err) {
        if (!mounted) return;
        setProducts([]);
        setSelectedIds([]);
        setTotalElements(0);
        setTotalPages(1);
        setError(err instanceof Error ? err.message : '상품 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadProducts();

    return () => {
      mounted = false;
    };
  }, [
    statusFilter,
    salesStatusFilter,
    displayStatusFilter,
    categoryIdFilter,
    stockStatusFilter,
    lowStockOnly,
    salePeriodStatusFilter,
    searchKeyword,
    page,
    reloadKey,
  ]);

  const allCurrentPageSelected = useMemo(
    () => products.length > 0 && products.every((product) => selectedIds.includes(product.id)),
    [products, selectedIds]
  );

  const handleSearch = () => {
    setSearchKeyword(keyword);
    setPage(1);
  };

  const resetFilters = () => {
    setKeyword('');
    setSearchKeyword('');
    setStatusFilter('ALL');
    setSalesStatusFilter('ALL');
    setDisplayStatusFilter('ALL');
    setCategoryIdFilter('ALL');
    setStockStatusFilter('ALL');
    setLowStockOnly(false);
    setSalePeriodStatusFilter('ALL');
    setPage(1);
  };

  const toggleProduct = (id: number) => {
    setSelectedIds((prev) => prev.includes(id) ? prev.filter((selectedId) => selectedId !== id) : [...prev, id]);
  };

  const toggleCurrentPage = () => {
    setSelectedIds(allCurrentPageSelected ? [] : products.map((product) => product.id));
  };

  const handleBulkUpdate = async () => {
    if (selectedIds.length === 0) return;
    if (bulkSalesStatus === 'KEEP' && bulkDisplayStatus === 'KEEP') {
      setBulkMessage('변경할 판매 상태 또는 전시 상태를 선택해주세요.');
      return;
    }

    setBulkLoading(true);
    setBulkMessage('');
    try {
      const res = await productService.bulkUpdateProductStatus({
        productIds: selectedIds,
        salesStatus: bulkSalesStatus !== 'KEEP' ? bulkSalesStatus as ProductSalesStatus : undefined,
        displayStatus: bulkDisplayStatus !== 'KEEP' ? bulkDisplayStatus as ProductDisplayStatus : undefined,
        reason: bulkReason.trim() || undefined,
      });
      setBulkMessage(`${res.updatedCount}개 상품 상태가 변경되었습니다.`);
      setSelectedIds([]);
      setBulkReason('');
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setBulkMessage(err instanceof Error ? err.message : '대량 상태 변경에 실패했습니다.');
    } finally {
      setBulkLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('상품을 삭제하시겠습니까?')) return;
    try {
      await productService.deleteProduct(id);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '상품 삭제에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="상품 관리">
      <div className="flex items-center justify-between mb-5">
        <p className="text-sm text-[#8a9bb5]">
          총 <span className="font-semibold text-[#1a1f2e]">{totalElements}</span>개 상품
        </p>
        <Link href="/admin/products/new">
          <Button variant="primary" size="sm">+ 상품 등록</Button>
        </Link>
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex flex-wrap items-center gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="상품명, 코드, 브랜드 검색"
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] flex-1 min-w-[200px]"
        />
        <select
          value={categoryIdFilter}
          onChange={(e) => { setCategoryIdFilter(e.target.value); setPage(1); }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white"
        >
          <option value="ALL">전체 카테고리</option>
          {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
        </select>
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(1); }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white"
        >
          <option value="ALL">전체 기존 상태</option>
          <option value="ON_SALE">판매중</option>
          <option value="SOLD_OUT">품절</option>
          <option value="HIDDEN">숨김</option>
        </select>
        <select
          value={salesStatusFilter}
          onChange={(e) => { setSalesStatusFilter(e.target.value); setPage(1); }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white"
        >
          <option value="ALL">전체 판매 상태</option>
          <option value="DRAFT">임시저장</option>
          <option value="ON_SALE">판매중</option>
          <option value="PAUSED">일시중지</option>
          <option value="SOLD_OUT">품절</option>
          <option value="DISCONTINUED">판매종료</option>
        </select>
        <select
          value={displayStatusFilter}
          onChange={(e) => { setDisplayStatusFilter(e.target.value); setPage(1); }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white"
        >
          <option value="ALL">전체 전시 상태</option>
          <option value="VISIBLE">노출</option>
          <option value="HIDDEN">숨김</option>
        </select>
        <Button variant="primary" size="sm" onClick={handleSearch}>검색</Button>
        <Button variant="outline" size="sm" onClick={resetFilters}>초기화</Button>
        <select
          value={stockStatusFilter}
          onChange={(e) => { setStockStatusFilter(e.target.value); setPage(1); }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white"
        >
          <option value="ALL">전체 재고</option>
          <option value="IN_STOCK">구매 가능</option>
          <option value="LOW_STOCK">품절 임박</option>
          <option value="SOLD_OUT">품절</option>
        </select>
        <select
          value={salePeriodStatusFilter}
          onChange={(e) => { setSalePeriodStatusFilter(e.target.value); setPage(1); }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white"
        >
          <option value="ALL">전체 판매 기간</option>
          <option value="ACTIVE">판매 기간 중</option>
          <option value="UPCOMING">판매 예정</option>
          <option value="ENDED">판매 종료</option>
        </select>
        <label className="inline-flex items-center gap-2 text-sm text-[#555]">
          <input
            type="checkbox"
            checked={lowStockOnly}
            onChange={(e) => { setLowStockOnly(e.target.checked); setPage(1); }}
            className="accent-[#222]"
          />
          안전 재고 이하만
        </label>
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <p className="text-sm text-[#4f5b70]">
            선택 상품 <span className="font-semibold text-[#1a1f2e]">{selectedIds.length}</span>개
          </p>
          <div className="flex flex-wrap items-center gap-2">
            <select value={bulkSalesStatus} onChange={(e) => setBulkSalesStatus(e.target.value)} className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white">
              <option value="KEEP">판매 상태 유지</option>
              <option value="DRAFT">임시저장</option>
              <option value="ON_SALE">판매중</option>
              <option value="PAUSED">일시중지</option>
              <option value="SOLD_OUT">품절</option>
              <option value="DISCONTINUED">판매종료</option>
            </select>
            <select value={bulkDisplayStatus} onChange={(e) => setBulkDisplayStatus(e.target.value)} className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white">
              <option value="KEEP">전시 상태 유지</option>
              <option value="VISIBLE">노출</option>
              <option value="HIDDEN">숨김</option>
            </select>
            <input value={bulkReason} onChange={(e) => setBulkReason(e.target.value)} placeholder="변경 사유 또는 운영 메모" className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] min-w-[220px]" />
            <Button variant="secondary" size="sm" disabled={selectedIds.length === 0 || bulkLoading} onClick={handleBulkUpdate}>
              {bulkLoading ? '변경 중...' : '선택 상태 변경'}
            </Button>
          </div>
        </div>
        {bulkMessage && <p className="text-xs text-[#4f5b70]">{bulkMessage}</p>}
        <label className="inline-flex items-center gap-2 text-sm text-[#555]">
          <input type="checkbox" checked={allCurrentPageSelected} onChange={toggleCurrentPage} className="accent-[#222]" />
          현재 페이지 전체 선택
        </label>
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
        <DataTable<ApiAdminProductItem>
          keyField="id"
          data={products}
          emptyMessage="상품 데이터가 없습니다."
          columns={[
            {
              key: 'select',
              header: '',
              render: (row) => (
                <input
                  type="checkbox"
                  checked={selectedIds.includes(row.id)}
                  onChange={() => toggleProduct(row.id)}
                  className="accent-[#222]"
                  aria-label={`${row.name} 선택`}
                />
              ),
            },
            {
              key: 'imageUrl',
              header: '이미지',
              render: (row) => (
                <div className="relative w-12 h-14 bg-[#f5f5f5]">
                  <Image
                    src={row.imageUrl || 'https://placehold.co/120x140?text=No+Image'}
                    alt={row.name}
                    fill
                    className="object-cover"
                    sizes="48px"
                  />
                </div>
              ),
            },
            {
              key: 'name',
              header: '상품명',
              render: (row) => (
                <div>
                  <p className="font-medium text-[#222] text-sm">{row.name}</p>
                  <p className="text-xs text-[#999] mt-0.5">
                    {row.productCode || '상품코드 없음'} · {row.brand || row.categoryName}
                  </p>
                </div>
              ),
            },
            {
              key: 'price',
              header: '판매가',
              render: (row) => <span className="font-medium">{formatPrice(row.price)}</span>,
            },
            {
              key: 'purchasePrice',
              header: '매입가',
              render: (row) => row.purchasePrice != null ? formatPrice(row.purchasePrice) : '-',
            },
            {
              key: 'marginRate',
              header: '마진율',
              render: (row) => `${Number(row.marginRate ?? 0).toFixed(2)}%`,
            },
            {
              key: 'stockSummary',
              header: '재고',
              render: (row) => (
                <div>
                  <p>{row.stockQuantity}개</p>
                  <p className="text-xs text-[#8a9bb5]">안전 {row.safetyStockQuantity ?? 0}개</p>
                  <p className="text-xs text-[#c47d19]">{row.stockDisplayText}</p>
                </div>
              ),
            },
            {
              key: 'statusBadge',
              header: '상태',
              render: (row) => (
                <div className="flex flex-col gap-1">
                  <span className={`text-xs font-medium px-2 py-0.5 ${PRODUCT_OPERATION_STATUS_COLOR[row.salesStatus] ?? ''}`}>
                    {PRODUCT_SALES_STATUS_LABEL[row.salesStatus] ?? row.salesStatus}
                  </span>
                  <span className={`text-xs font-medium px-2 py-0.5 ${PRODUCT_OPERATION_STATUS_COLOR[row.displayStatus] ?? ''}`}>
                    {PRODUCT_DISPLAY_STATUS_LABEL[row.displayStatus] ?? row.displayStatus}
                  </span>
                </div>
              ),
            },
            {
              key: 'actions',
              header: '관리',
              render: (row) => (
                <div className="flex gap-2">
                  <Link href={`/admin/products/${row.id}`}>
                    <Button variant="outline" size="sm">수정</Button>
                  </Link>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDelete(row.id)}
                    className="text-red-500 hover:bg-red-50"
                  >
                    삭제
                  </Button>
                </div>
              ),
            },
          ]}
        />
      )}

      {!loading && !error && (
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      )}
    </AdminLayout>
  );
}
