'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { productService, type ApiProductItem } from '@/lib/services/productService';
import { formatPrice, PRODUCT_STATUS_LABEL, PRODUCT_STATUS_COLOR } from '@/lib/format';

const PAGE_SIZE = 8;

export default function AdminProductsPage() {
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [page, setPage] = useState(1);
  const [products, setProducts] = useState<ApiProductItem[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const loadProducts = async () => {
      setLoading(true);
      setError('');

      try {
        const res = await productService.getAdminProducts({
          status: statusFilter,
          keyword: searchKeyword || undefined,
          page: page - 1,
          size: PAGE_SIZE,
        });
        if (!mounted) return;
        setProducts(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages || 1);
      } catch (err) {
        if (!mounted) return;
        setProducts([]);
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
  }, [statusFilter, searchKeyword, page, reloadKey]);

  const handleSearch = () => {
    setSearchKeyword(keyword);
    setPage(1);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return;
    try {
      await productService.deleteProduct(id);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '삭제에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="상품 관리">
      <div className="flex items-center justify-between mb-5">
        <p className="text-sm text-[#8a9bb5]">
          총 <span className="font-semibold text-[#1a1f2e]">{totalElements}</span>개 상품
        </p>
        <Link href="/admin/products/new">
          <Button variant="primary" size="sm">
            + 상품 등록
          </Button>
        </Link>
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex flex-wrap items-center gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="상품명 검색"
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] flex-1 min-w-[200px]"
        />
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(1); }}
          className="border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e] bg-white"
        >
          <option value="ALL">전체 상태</option>
          <option value="ON_SALE">판매중</option>
          <option value="SOLD_OUT">품절</option>
          <option value="HIDDEN">숨김</option>
        </select>
        <Button variant="primary" size="sm" onClick={handleSearch}>
          검색
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
        <DataTable<ApiProductItem>
          keyField="id"
          data={products}
          emptyMessage="상품 데이터가 없습니다."
          columns={[
            {
              key: 'imageUrl',
              header: '이미지',
              render: (row) => (
                <div className="relative w-12 h-14 bg-[#f5f5f5]">
                  <Image src={row.imageUrl} alt={row.name} fill className="object-cover" sizes="48px" />
                </div>
              ),
            },
            {
              key: 'name',
              header: '상품명',
              render: (row) => (
                <div>
                  <p className="font-medium text-[#222] text-sm">{row.name}</p>
                  <p className="text-xs text-[#999] mt-0.5">{row.categoryName}</p>
                </div>
              ),
            },
            {
              key: 'price',
              header: '판매가',
              render: (row) => <span className="font-medium">{formatPrice(row.price)}</span>,
            },
            { key: 'stockQuantity', header: '재고', render: (row) => `${row.stockQuantity}개` },
            {
              key: 'status',
              header: '상태',
              render: (row) => (
                <span className={`text-xs font-medium px-2 py-0.5 ${PRODUCT_STATUS_COLOR[row.status] ?? ''}`}>
                  {PRODUCT_STATUS_LABEL[row.status] ?? row.status}
                </span>
              ),
            },
            {
              key: 'id',
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
