'use client';

import { use, useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import ProductImageUpload from '@/components/admin/ProductImageUpload';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import { productService, type ApiCategory, type ApiProductDetail, type ProductOptionGroup } from '@/lib/services/productService';

interface OptionGroupDraft {
  name: string;
  valueInput: string;
  values: string[];
}

export default function AdminProductEditPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const [categories, setCategories] = useState<ApiCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [notFound, setNotFound] = useState(false);
  const [optionGroups, setOptionGroups] = useState<OptionGroupDraft[]>([]);
  const [form, setForm] = useState({
    categoryId: '',
    name: '',
    price: '',
    stockQuantity: '',
    description: '',
    imageUrl: '',
    status: 'ON_SALE',
  });

  useEffect(() => {
    Promise.all([
      productService.getAdminProduct(Number(id)),
      productService.getCategories(),
    ])
      .then(([product, cats]: [ApiProductDetail, ApiCategory[]]) => {
        setCategories(cats);
        setForm({
          categoryId: String(product.categoryId),
          name: product.name,
          price: String(product.price),
          stockQuantity: String(product.stockQuantity),
          description: product.description ?? '',
          imageUrl: product.imageUrl ?? '',
          status: product.status,
        });
        setOptionGroups((product.options ?? []).map((option) => ({
          name: option.name,
          valueInput: '',
          values: option.values,
        })));
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [id]);

  const set = (field: string, value: string) =>
    setForm((prev) => ({ ...prev, [field]: value }));

  const addOptionGroup = () =>
    setOptionGroups((prev) => [...prev, { name: '', valueInput: '', values: [] }]);

  const removeOptionGroup = (idx: number) =>
    setOptionGroups((prev) => prev.filter((_, i) => i !== idx));

  const updateOptionGroupName = (idx: number, name: string) =>
    setOptionGroups((prev) => prev.map((og, i) => i === idx ? { ...og, name } : og));

  const updateOptionGroupValueInput = (idx: number, val: string) =>
    setOptionGroups((prev) => prev.map((og, i) => i === idx ? { ...og, valueInput: val } : og));

  const addOptionValue = (idx: number) => {
    setOptionGroups((prev) => prev.map((og, i) => {
      if (i !== idx) return og;
      const trimmed = og.valueInput.trim();
      if (!trimmed || og.values.includes(trimmed)) return { ...og, valueInput: '' };
      return { ...og, values: [...og.values, trimmed], valueInput: '' };
    }));
  };

  const removeOptionValue = (groupIdx: number, val: string) =>
    setOptionGroups((prev) => prev.map((og, i) =>
      i === groupIdx ? { ...og, values: og.values.filter((v) => v !== val) } : og
    ));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name.trim() || !form.price || !form.categoryId) {
      alert('상품명, 카테고리, 판매가는 필수입니다.');
      return;
    }
    const options: ProductOptionGroup[] = optionGroups
      .filter((og) => og.name.trim() && og.values.length > 0)
      .map((og) => ({ name: og.name.trim(), values: og.values }));
    setSubmitting(true);
    try {
      await productService.updateProduct(Number(id), {
        categoryId: Number(form.categoryId),
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity),
        imageUrl: form.imageUrl.trim() || undefined,
        status: form.status,
        options,
      });
      alert('상품이 수정되었습니다.');
      router.push('/admin/products');
    } catch (err) {
      alert(err instanceof Error ? err.message : '상품 수정에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <AdminLayout title="상품 수정">
        <div className="py-12 text-center text-[#bbb] text-sm">불러오는 중...</div>
      </AdminLayout>
    );
  }

  if (notFound) {
    return (
      <AdminLayout title="상품 수정">
        <div className="py-12 text-center text-[#bbb] text-sm">
          상품을 찾을 수 없습니다.
          <br />
          <Link href="/admin/products" className="text-[#1a1f2e] underline mt-2 inline-block">
            목록으로
          </Link>
        </div>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout title="상품 수정">
      <div className="max-w-[720px]">
        <div className="flex items-center gap-3 mb-6">
          <Link href="/admin/products" className="text-sm text-[#8a9bb5] hover:text-[#1a1f2e] transition-colors">
            ← 상품 목록
          </Link>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="bg-white border border-[#e8eaf0] p-6 space-y-5">
            <h2 className="text-sm font-bold text-[#1a1f2e] pb-3 border-b border-[#f0f1f5]">기본 정보</h2>

            <Input
              label="상품명 *"
              value={form.name}
              onChange={(e) => set('name', e.target.value)}
              placeholder="상품명 입력"
              fullWidth
            />

            <div>
              <label className="block text-sm font-medium text-[#444] mb-1">카테고리 *</label>
              <select
                value={form.categoryId}
                onChange={(e) => set('categoryId', e.target.value)}
                className="border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] w-full"
              >
                <option value="">카테고리 선택</option>
                {categories.map((cat) => (
                  <option key={cat.id} value={cat.id}>{cat.name}</option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="판매가 (원) *"
                type="number"
                value={form.price}
                onChange={(e) => set('price', e.target.value)}
                placeholder="예) 35000"
                fullWidth
              />
              <Input
                label="재고 수량"
                type="number"
                value={form.stockQuantity}
                onChange={(e) => set('stockQuantity', e.target.value)}
                placeholder="예) 100"
                fullWidth
              />
            </div>

            <ProductImageUpload
              imageUrl={form.imageUrl}
              onImageUrlChange={(url) => set('imageUrl', url)}
            />

            <div>
              <label className="block text-sm font-medium text-[#444] mb-1">판매 상태</label>
              <div className="flex gap-4">
                {[
                  { value: 'ON_SALE', label: '판매중' },
                  { value: 'HIDDEN', label: '숨김' },
                  { value: 'SOLD_OUT', label: '품절' },
                ].map((opt) => (
                  <label key={opt.value} className="flex items-center gap-2 cursor-pointer">
                    <input
                      type="radio"
                      name="status"
                      value={opt.value}
                      checked={form.status === opt.value}
                      onChange={(e) => set('status', e.target.value)}
                      className="accent-[#222]"
                    />
                    <span className="text-sm text-[#555]">{opt.label}</span>
                  </label>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-[#444] mb-1">상품 설명</label>
              <textarea
                value={form.description}
                onChange={(e) => set('description', e.target.value)}
                placeholder="상품 상세 설명 입력"
                rows={5}
                className="w-full border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] resize-none"
              />
            </div>

            <div className="pt-2">
              <div className="flex items-center justify-between pb-3 border-b border-[#f0f1f5]">
                <h2 className="text-sm font-bold text-[#1a1f2e]">상품 옵션</h2>
                <Button variant="outline" size="sm" type="button" onClick={addOptionGroup}>
                  옵션 그룹 추가
                </Button>
              </div>

              {optionGroups.length === 0 ? (
                <p className="py-5 text-sm text-[#8a9bb5]">
                  색상, 사이즈처럼 구매자가 선택할 옵션이 있으면 추가하세요.
                </p>
              ) : (
                <div className="space-y-4 pt-4">
                  {optionGroups.map((og, idx) => (
                    <div key={idx} className="border border-[#e8eaf0] p-4 space-y-3">
                      <div className="flex items-end gap-3">
                        <Input
                          label="옵션명"
                          value={og.name}
                          onChange={(e) => updateOptionGroupName(idx, e.target.value)}
                          placeholder="예: 색상"
                          fullWidth
                        />
                        <Button
                          variant="ghost"
                          size="sm"
                          type="button"
                          onClick={() => removeOptionGroup(idx)}
                          className="mb-0.5 shrink-0 text-[#d94f4f]"
                        >
                          삭제
                        </Button>
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-[#444] mb-1">옵션값</label>
                        <div className="flex gap-2">
                          <input
                            value={og.valueInput}
                            onChange={(e) => updateOptionGroupValueInput(idx, e.target.value)}
                            onKeyDown={(e) => {
                              if (e.key === 'Enter') {
                                e.preventDefault();
                                addOptionValue(idx);
                              }
                            }}
                            placeholder="예: 블랙"
                            className="flex-1 border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222]"
                          />
                          <Button variant="outline" type="button" onClick={() => addOptionValue(idx)}>
                            추가
                          </Button>
                        </div>
                      </div>

                      {og.values.length > 0 && (
                        <div className="flex flex-wrap gap-2">
                          {og.values.map((value) => (
                            <button
                              key={value}
                              type="button"
                              onClick={() => removeOptionValue(idx, value)}
                              className="border border-[#d8dce6] bg-[#f7f8fc] px-3 py-1 text-xs text-[#4f5b70] hover:border-[#d94f4f] hover:text-[#d94f4f]"
                            >
                              {value} ×
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 mt-5">
            <Link href="/admin/products">
              <Button variant="outline" type="button">취소</Button>
            </Link>
            <Button variant="primary" type="submit" disabled={submitting}>
              {submitting ? '저장 중...' : '수정 완료'}
            </Button>
          </div>
        </form>
      </div>
    </AdminLayout>
  );
}
