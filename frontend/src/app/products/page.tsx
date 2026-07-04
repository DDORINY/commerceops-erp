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

  // Filter state
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [sortBy, setSortBy] = useState('');
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [inStock, setInStock] = useState(false);
  const [showFilter, setShowFilter] = useState(false);

  // active filter count for badge
  const activeFilterCount = [
    keyword,
    minPrice,
    maxPrice,
    inStock,
  ].filter(Boolean).length;

  useEffect(() => {
    productService.getCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  const fetchProducts = useCallback(() => {
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
      })
      .catch(() => setProducts([]))
      .finally(() => setLoading(false));
  }, [categories, selectedCategory, keyword, sortBy, minPrice, maxPrice, inStock, page]);

  useEffect(() => {
    fetchProducts();
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

  const handlePriceApply = () => {
    setPage(1);
    fetchProducts();
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
        {/* 헤더 + 검색바 */}
        <div className="mb-8">
          <div className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4 mb-6">
            <div>
              <h1 className="text-xl font-bold text-[#222]">
                {selectedCategory === 'ALL' ? '전체 상품' : selectedCategory}
              </h1>
              <p className="text-sm text-[#999] mt-1">
                {loading ? '검색 중...' : `총 ${totalElements.toLocaleString()}개 상품`}
              </p>
            </div>

            {/* 검색바 */}
            <form onSubmit={handleSearch} className="flex gap-2">
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="상품명 검색"
                className="border border-[#ddd] text-sm px-3 py-2 w-52 outline-none focus:border-[#222] text-[#444]"
              />
              <button
                type="submit"
                className="border border-[#222] bg-[#222] text-white text-sm px-4 py-2 hover:bg-[#444] transition-colors"
              >
                검색
              </button>
            </form>
          </div>

          {/* 카테고리 탭 */}
          <div className="flex flex-wrap gap-2 mb-4">
            <button
              onClick={() => handleCategoryChange('ALL')}
              className={[
                'px-4 py-1.5 text-sm border transition-colors',
                selectedCategory === 'ALL'
                  ? 'border-[#222] bg-[#222] text-white'
                  : 'border-[#ddd] text-[#555] hover:border-[#222]',
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
                  selectedCategory === cat.name
                    ? 'border-[#222] bg-[#222] text-white'
                    : 'border-[#ddd] text-[#555] hover:border-[#222]',
                ].join(' ')}
              >
                {cat.name}
              </button>
            ))}
          </div>

          {/* 필터 + 정렬 바 */}
          <div className="flex items-center justify-between border-t border-[#f0f0f0] pt-4">
            <div className="flex items-center gap-3">
              <button
                onClick={() => setShowFilter((v) => !v)}
                className={[
                  'flex items-center gap-1.5 text-sm px-3 py-1.5 border transition-colors',
                  showFilter || activeFilterCount > 0
                    ? 'border-[#222] bg-[#222] text-white'
                    : 'border-[#ddd] text-[#555] hover:border-[#222]',
                ].join(' ')}
              >
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 010 2H4a1 1 0 01-1-1zm3 6a1 1 0 011-1h10a1 1 0 010 2H7a1 1 0 01-1-1zm4 6a1 1 0 011-1h2a1 1 0 010 2h-2a1 1 0 01-1-1z" />
                </svg>
                필터
                {activeFilterCount > 0 && (
                  <span className="ml-0.5 bg-white text-[#222] text-xs font-bold w-4 h-4 flex items-center justify-center rounded-full leading-none">
                    {activeFilterCount}
                  </span>
                )}
              </button>

              {/* 활성 필터 태그 */}
              {keyword && (
                <span className="flex items-center gap-1 text-xs bg-[#f5f5f5] px-2.5 py-1 text-[#444]">
                  &ldquo;{keyword}&rdquo;
                  <button onClick={() => { setKeyword(''); setSearchInput(''); setPage(1); }} className="text-[#999] hover:text-[#222]">×</button>
                </span>
              )}
              {(minPrice || maxPrice) && (
                <span className="flex items-center gap-1 text-xs bg-[#f5f5f5] px-2.5 py-1 text-[#444]">
                  {minPrice ? formatPrice(Number(minPrice)) : '0'} ~ {maxPrice ? formatPrice(Number(maxPrice)) : '무제한'}
                  <button onClick={() => { setMinPrice(''); setMaxPrice(''); setPage(1); }} className="text-[#999] hover:text-[#222]">×</button>
                </span>
              )}
              {inStock && (
                <span className="flex items-center gap-1 text-xs bg-[#f5f5f5] px-2.5 py-1 text-[#444]">
                  재고있음
                  <button onClick={() => { setInStock(false); setPage(1); }} className="text-[#999] hover:text-[#222]">×</button>
                </span>
              )}
              {activeFilterCount > 0 && (
                <button onClick={handleReset} className="text-xs text-[#999] hover:text-[#444] underline">
                  전체 초기화
                </button>
              )}
            </div>

            <select
              value={sortBy}
              onChange={(e) => { setSortBy(e.target.value); setPage(1); }}
              className="border border-[#ddd] text-sm px-3 py-1.5 text-[#444] bg-white outline-none focus:border-[#222]"
            >
              {SORT_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>

          {/* 필터 패널 */}
          {showFilter && (
            <div className="mt-3 border border-[#e5e5e5] bg-[#fafafa] p-5">
              <div className="flex flex-wrap gap-8">
                {/* 가격 범위 */}
                <div>
                  <p className="text-xs font-bold text-[#333] mb-3 uppercase tracking-wide">가격 범위</p>
                  <div className="flex items-center gap-2">
                    <input
                      type="number"
                      value={minPrice}
                      onChange={(e) => setMinPrice(e.target.value)}
                      placeholder="최소"
                      min={0}
                      className="w-28 border border-[#ddd] px-3 py-1.5 text-sm outline-none focus:border-[#222] bg-white"
                    />
                    <span className="text-[#999] text-sm">~</span>
                    <input
                      type="number"
                      value={maxPrice}
                      onChange={(e) => setMaxPrice(e.target.value)}
                      placeholder="최대"
                      min={0}
                      className="w-28 border border-[#ddd] px-3 py-1.5 text-sm outline-none focus:border-[#222] bg-white"
                    />
                    <span className="text-sm text-[#999]">원</span>
                    <button
                      onClick={handlePriceApply}
                      className="border border-[#222] text-[#222] text-xs px-3 py-1.5 hover:bg-[#222] hover:text-white transition-colors"
                    >
                      적용
                    </button>
                  </div>
                </div>

                {/* 재고 필터 */}
                <div>
                  <p className="text-xs font-bold text-[#333] mb-3 uppercase tracking-wide">재고</p>
                  <label className="flex items-center gap-2 cursor-pointer text-sm text-[#555]">
                    <input
                      type="checkbox"
                      checked={inStock}
                      onChange={(e) => { setInStock(e.target.checked); setPage(1); }}
                      className="w-4 h-4 accent-[#222]"
                    />
                    재고 있는 상품만
                  </label>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 상품 그리드 */}
        {loading ? (
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
