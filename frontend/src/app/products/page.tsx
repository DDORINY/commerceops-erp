'use client';

import { useState, useEffect, useCallback } from 'react';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import ProductGrid from '@/components/shop/ProductGrid';
import Pagination from '@/components/common/Pagination';
import { productService, toProductListItem, type ApiCategory } from '@/lib/services/productService';
import { formatPrice } from '@/lib/format';
import type { ProductListItem } from '@/features/product/types';

const SORT_OPTIONS = [
  { value: '', label: '신상품순' },
  { value: 'priceAsc', label: '낮은 가격순' },
  { value: 'priceDesc', label: '높은 가격순' },
];

const PAGE_SIZE = 12;

export default function ProductsPage() {
  const [categories, setCategories] = useState<ApiCategory[]>([]);
  const [products, setProducts] = useState<ProductListItem[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [sortBy, setSortBy] = useState('');
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [inStock, setInStock] = useState(false);
  const [showFilter, setShowFilter] = useState(false);

  const activeFilterCount = [keyword, minPrice, maxPrice, inStock].filter(Boolean).length;

  useEffect(() => {
    productService.getCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    queueMicrotask(() => {
      const query = new URLSearchParams(window.location.search);
      const category = query.get('category');
      const queryKeyword = query.get('keyword');
      setSelectedCategory(category || 'ALL');
      setKeyword(queryKeyword || '');
      setSearchInput(queryKeyword || '');
      setPage(1);
    });
  }, []);

  const fetchProducts = useCallback(() => {
    setLoading(true);
    const category = categories.find((c) => c.name === selectedCategory);
    productService
      .getProducts({
        categoryId: category ? category.id : undefined,
        keyword: keyword || undefined,
        sort: sortBy || undefined,
        minPrice: minPrice ? Number(minPrice) : undefined,
        maxPrice: maxPrice ? Number(maxPrice) : undefined,
        inStock: inStock || undefined,
        page: page - 1,
        size: PAGE_SIZE,
      })
      .then((res) => {
        setProducts(res.content.map(toProductListItem));
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages || 1);
        setErrorMessage('');
      })
      .catch(() => {
        setProducts([]);
        setTotalElements(0);
        setTotalPages(1);
        setErrorMessage('상품 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
      })
      .finally(() => setLoading(false));
  }, [categories, selectedCategory, keyword, sortBy, minPrice, maxPrice, inStock, page]);

  useEffect(() => {
    queueMicrotask(fetchProducts);
  }, [fetchProducts]);

  const handleCategoryChange = (cat: string) => {
    setSelectedCategory(cat);
    setPage(1);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setKeyword(searchInput.trim());
    setPage(1);
  };

  const handleReset = () => {
    setKeyword('');
    setSearchInput('');
    setMinPrice('');
    setMaxPrice('');
    setInStock(false);
    setSortBy('');
    setSelectedCategory('ALL');
    setPage(1);
  };

  return (
    <>
      <ShopHeader />

      <main className="max-w-[1200px] mx-auto px-4 py-10">
        <div className="mb-8">
          <div className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4 mb-6">
            <div>
              <h1 className="text-xl font-bold text-[#222]">{selectedCategory === 'ALL' ? '전체 상품' : selectedCategory}</h1>
              <p className="text-sm text-[#999] mt-1">{loading ? '검색 중...' : `총 ${totalElements.toLocaleString()}개 상품`}</p>
            </div>

            <form onSubmit={handleSearch} className="flex gap-2">
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="상품명 검색"
                className="border border-[#ddd] text-sm px-3 py-2 w-52 outline-none focus:border-[#222] text-[#444]"
              />
              <button type="submit" className="border border-[#222] bg-[#222] text-white text-sm px-4 py-2 hover:bg-[#444] transition-colors">
                검색
              </button>
            </form>
          </div>

          <div className="flex flex-wrap gap-2 mb-4">
            <button
              onClick={() => handleCategoryChange('ALL')}
              className={[
                'px-4 py-1.5 text-sm border transition-colors',
                selectedCategory === 'ALL' ? 'border-[#222] bg-[#222] text-white' : 'border-[#ddd] text-[#555] hover:border-[#222]',
              ].join(' ')}
            >
              전체
            </button>
            {categories.map((cat) => (
              <button
                key={cat.id}
                onClick={() => handleCategoryChange(cat.name)}
                className={[
                  'px-4 py-1.5 text-sm border transition-colors',
                  selectedCategory === cat.name ? 'border-[#222] bg-[#222] text-white' : 'border-[#ddd] text-[#555] hover:border-[#222]',
                ].join(' ')}
              >
                {cat.name}
              </button>
            ))}
          </div>

          <div className="flex items-center justify-between border-t border-[#f0f0f0] pt-4">
            <div className="flex flex-wrap items-center gap-3">
              <button
                onClick={() => setShowFilter((v) => !v)}
                className={[
                  'flex items-center gap-1.5 text-sm px-3 py-1.5 border transition-colors',
                  showFilter || activeFilterCount > 0 ? 'border-[#222] bg-[#222] text-white' : 'border-[#ddd] text-[#555] hover:border-[#222]',
                ].join(' ')}
              >
                필터
                {activeFilterCount > 0 && <span className="ml-0.5 bg-white text-[#222] text-xs font-bold w-4 h-4 flex items-center justify-center rounded-full leading-none">{activeFilterCount}</span>}
              </button>

              {keyword && (
                <span className="flex items-center gap-1 text-xs bg-[#f5f5f5] px-2.5 py-1 text-[#444]">
                  &ldquo;{keyword}&rdquo;
                  <button onClick={() => { setKeyword(''); setSearchInput(''); setPage(1); }} className="text-[#999] hover:text-[#222]">×</button>
                </span>
              )}
              {(minPrice || maxPrice) && (
                <span className="flex items-center gap-1 text-xs bg-[#f5f5f5] px-2.5 py-1 text-[#444]">
                  {minPrice ? formatPrice(Number(minPrice)) : '0원'} ~ {maxPrice ? formatPrice(Number(maxPrice)) : '제한 없음'}
                  <button onClick={() => { setMinPrice(''); setMaxPrice(''); setPage(1); }} className="text-[#999] hover:text-[#222]">×</button>
                </span>
              )}
              {inStock && (
                <span className="flex items-center gap-1 text-xs bg-[#f5f5f5] px-2.5 py-1 text-[#444]">
                  재고 있음
                  <button onClick={() => { setInStock(false); setPage(1); }} className="text-[#999] hover:text-[#222]">×</button>
                </span>
              )}
              {activeFilterCount > 0 && <button onClick={handleReset} className="text-xs text-[#999] hover:text-[#444] underline">전체 초기화</button>}
            </div>

            <select value={sortBy} onChange={(e) => { setSortBy(e.target.value); setPage(1); }} className="border border-[#ddd] text-sm px-3 py-1.5 text-[#444] bg-white outline-none focus:border-[#222]">
              {SORT_OPTIONS.map((opt) => <option key={opt.value} value={opt.value}>{opt.label}</option>)}
            </select>
          </div>

          {showFilter && (
            <div className="mt-3 border border-[#e5e5e5] bg-[#fafafa] p-5">
              <div className="flex flex-wrap gap-8">
                <div>
                  <p className="text-xs font-bold text-[#333] mb-3 uppercase tracking-wide">가격 범위</p>
                  <div className="flex items-center gap-2">
                    <input type="number" value={minPrice} onChange={(e) => setMinPrice(e.target.value)} placeholder="최소" min={0} className="w-28 border border-[#ddd] px-3 py-1.5 text-sm outline-none focus:border-[#222] bg-white" />
                    <span className="text-[#999] text-sm">~</span>
                    <input type="number" value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)} placeholder="최대" min={0} className="w-28 border border-[#ddd] px-3 py-1.5 text-sm outline-none focus:border-[#222] bg-white" />
                    <span className="text-sm text-[#999]">원</span>
                  </div>
                </div>

                <div>
                  <p className="text-xs font-bold text-[#333] mb-3 uppercase tracking-wide">재고</p>
                  <label className="flex items-center gap-2 cursor-pointer text-sm text-[#555]">
                    <input type="checkbox" checked={inStock} onChange={(e) => { setInStock(e.target.checked); setPage(1); }} className="w-4 h-4 accent-[#222]" />
                    재고 있는 상품만
                  </label>
                </div>
              </div>
            </div>
          )}
        </div>

        {errorMessage ? (
          <div className="py-20 text-center">
            <p className="text-[#c43a3a] text-sm mb-4">{errorMessage}</p>
            <button onClick={handleReset} className="text-sm text-[#555] underline">필터 초기화</button>
          </div>
        ) : loading ? (
          <div className="py-20 text-center text-[#aaa] text-sm">상품을 불러오는 중...</div>
        ) : products.length === 0 ? (
          <div className="py-20 text-center">
            <p className="text-[#bbb] text-sm mb-4">검색 결과가 없습니다.</p>
            <button onClick={handleReset} className="text-sm text-[#555] underline">필터 초기화</button>
          </div>
        ) : (
          <ProductGrid products={products} columns={4} />
        )}

        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      </main>

      <ShopFooter />
    </>
  );
}
