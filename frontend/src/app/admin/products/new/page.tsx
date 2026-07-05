'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import ProductImageUpload from '@/components/admin/ProductImageUpload';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import {
  productService,
  type ApiCategory,
  type ProductDisplayStatus,
  type ProductOptionGroup,
  type ProductSalesStatus,
} from '@/lib/services/productService';

interface OptionGroupDraft {
  name: string;
  valueInput: string;
  values: string[];
}

const optionalNumber = (value: string) => value.trim() ? Number(value) : undefined;
const optionalDateTime = (value: string) => value.trim() ? value : undefined;

export default function AdminProductNewPage() {
  const router = useRouter();
  const [categories, setCategories] = useState<ApiCategory[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [optionGroups, setOptionGroups] = useState<OptionGroupDraft[]>([]);
  const [form, setForm] = useState({
    categoryId: '',
    name: '',
    productCode: '',
    brand: '',
    manufacturer: '',
    modelName: '',
    origin: '',
    price: '',
    originalPrice: '',
    discountPrice: '',
    purchasePrice: '',
    stockQuantity: '',
    safetyStockQuantity: '5',
    searchKeywords: '',
    tags: '',
    saleStartAt: '',
    saleEndAt: '',
    deliveryInfo: '',
    seoTitle: '',
    seoDescription: '',
    seoKeywords: '',
    description: '',
    imageUrl: '',
    status: 'ON_SALE',
    salesStatus: 'ON_SALE',
    displayStatus: 'VISIBLE',
  });

  useEffect(() => {
    productService.getCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  const set = (field: string, value: string) => setForm((prev) => ({ ...prev, [field]: value }));

  const addOptionGroup = () => setOptionGroups((prev) => [...prev, { name: '', valueInput: '', values: [] }]);
  const removeOptionGroup = (idx: number) => setOptionGroups((prev) => prev.filter((_, i) => i !== idx));
  const updateOptionGroupName = (idx: number, name: string) =>
    setOptionGroups((prev) => prev.map((og, i) => i === idx ? { ...og, name } : og));
  const updateOptionGroupValueInput = (idx: number, valueInput: string) =>
    setOptionGroups((prev) => prev.map((og, i) => i === idx ? { ...og, valueInput } : og));
  const addOptionValue = (idx: number) => {
    setOptionGroups((prev) => prev.map((og, i) => {
      if (i !== idx) return og;
      const value = og.valueInput.trim();
      if (!value || og.values.includes(value)) return { ...og, valueInput: '' };
      return { ...og, values: [...og.values, value], valueInput: '' };
    }));
  };
  const removeOptionValue = (idx: number, value: string) =>
    setOptionGroups((prev) => prev.map((og, i) => i === idx ? { ...og, values: og.values.filter((v) => v !== value) } : og));

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
      await productService.createProduct({
        categoryId: Number(form.categoryId),
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        price: Number(form.price),
        productCode: form.productCode.trim() || undefined,
        brand: form.brand.trim() || undefined,
        manufacturer: form.manufacturer.trim() || undefined,
        modelName: form.modelName.trim() || undefined,
        origin: form.origin.trim() || undefined,
        originalPrice: optionalNumber(form.originalPrice),
        discountPrice: optionalNumber(form.discountPrice),
        purchasePrice: optionalNumber(form.purchasePrice),
        searchKeywords: form.searchKeywords.trim() || undefined,
        tags: form.tags.trim() || undefined,
        saleStartAt: optionalDateTime(form.saleStartAt),
        saleEndAt: optionalDateTime(form.saleEndAt),
        deliveryInfo: form.deliveryInfo.trim() || undefined,
        seoTitle: form.seoTitle.trim() || undefined,
        seoDescription: form.seoDescription.trim() || undefined,
        seoKeywords: form.seoKeywords.trim() || undefined,
        stockQuantity: Number(form.stockQuantity) || 0,
        safetyStockQuantity: optionalNumber(form.safetyStockQuantity),
        imageUrl: form.imageUrl.trim() || undefined,
        status: form.status,
        salesStatus: form.salesStatus as ProductSalesStatus,
        displayStatus: form.displayStatus as ProductDisplayStatus,
        options: options.length > 0 ? options : undefined,
      });
      alert('상품이 등록되었습니다.');
      router.push('/admin/products');
    } catch (err) {
      alert(err instanceof Error ? err.message : '상품 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AdminLayout title="상품 등록">
      <div className="max-w-[760px]">
        <Link href="/admin/products" className="text-sm text-[#8a9bb5] hover:text-[#1a1f2e] transition-colors">
          ← 상품 목록
        </Link>

        <form onSubmit={handleSubmit} className="mt-6">
          <div className="bg-white border border-[#e8eaf0] p-6 space-y-6">
            <section className="space-y-4">
              <h2 className="text-sm font-bold text-[#1a1f2e] pb-3 border-b border-[#f0f1f5]">기본 정보</h2>
              <Input label="상품명 *" value={form.name} onChange={(e) => set('name', e.target.value)} placeholder="상품명 입력" fullWidth />
              <div>
                <label className="block text-sm font-medium text-[#444] mb-1">카테고리 *</label>
                <select value={form.categoryId} onChange={(e) => set('categoryId', e.target.value)} className="border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] w-full">
                  <option value="">카테고리 선택</option>
                  {categories.map((cat) => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <Input label="판매가 *" type="number" value={form.price} onChange={(e) => set('price', e.target.value)} placeholder="35000" fullWidth />
                <Input label="재고 수량" type="number" value={form.stockQuantity} onChange={(e) => set('stockQuantity', e.target.value)} placeholder="100" fullWidth />
              </div>
            </section>

            <section className="space-y-4">
              <h2 className="text-sm font-bold text-[#1a1f2e] pb-3 border-b border-[#f0f1f5]">판매/전시 운영</h2>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-[#444] mb-1">판매 상태</label>
                  <select value={form.salesStatus} onChange={(e) => set('salesStatus', e.target.value)} className="border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] w-full">
                    <option value="DRAFT">임시저장</option>
                    <option value="ON_SALE">판매중</option>
                    <option value="PAUSED">일시중지</option>
                    <option value="SOLD_OUT">품절</option>
                    <option value="DISCONTINUED">판매종료</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-[#444] mb-1">전시 상태</label>
                  <select value={form.displayStatus} onChange={(e) => set('displayStatus', e.target.value)} className="border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] w-full">
                    <option value="VISIBLE">노출</option>
                    <option value="HIDDEN">숨김</option>
                  </select>
                </div>
                <Input label="안전 재고" type="number" value={form.safetyStockQuantity} onChange={(e) => set('safetyStockQuantity', e.target.value)} placeholder="5" fullWidth />
              </div>
            </section>

            <section className="space-y-4">
              <h2 className="text-sm font-bold text-[#1a1f2e] pb-3 border-b border-[#f0f1f5]">상품 마스터</h2>
              <div className="grid grid-cols-2 gap-4">
                <Input label="상품코드" value={form.productCode} onChange={(e) => set('productCode', e.target.value)} placeholder="SKU-001" fullWidth />
                <Input label="브랜드" value={form.brand} onChange={(e) => set('brand', e.target.value)} placeholder="브랜드명" fullWidth />
                <Input label="제조사" value={form.manufacturer} onChange={(e) => set('manufacturer', e.target.value)} placeholder="제조사" fullWidth />
                <Input label="모델명" value={form.modelName} onChange={(e) => set('modelName', e.target.value)} placeholder="모델명" fullWidth />
                <Input label="원산지" value={form.origin} onChange={(e) => set('origin', e.target.value)} placeholder="대한민국" fullWidth />
              </div>
            </section>

            <section className="space-y-4">
              <h2 className="text-sm font-bold text-[#1a1f2e] pb-3 border-b border-[#f0f1f5]">가격/검색/SEO</h2>
              <div className="grid grid-cols-2 gap-4">
                <Input label="정상가" type="number" value={form.originalPrice} onChange={(e) => set('originalPrice', e.target.value)} placeholder="50000" fullWidth />
                <Input label="할인 금액" type="number" value={form.discountPrice} onChange={(e) => set('discountPrice', e.target.value)} placeholder="5000" fullWidth />
                <Input label="매입가" type="number" value={form.purchasePrice} onChange={(e) => set('purchasePrice', e.target.value)} placeholder="25000" fullWidth />
              </div>
              <Input label="검색 키워드" value={form.searchKeywords} onChange={(e) => set('searchKeywords', e.target.value)} placeholder="쉼표로 구분" fullWidth />
              <Input label="태그" value={form.tags} onChange={(e) => set('tags', e.target.value)} placeholder="쉼표로 구분" fullWidth />
              <div className="grid grid-cols-2 gap-4">
                <Input label="판매 시작일" type="datetime-local" value={form.saleStartAt} onChange={(e) => set('saleStartAt', e.target.value)} fullWidth />
                <Input label="판매 종료일" type="datetime-local" value={form.saleEndAt} onChange={(e) => set('saleEndAt', e.target.value)} fullWidth />
              </div>
              <Input label="배송 정보" value={form.deliveryInfo} onChange={(e) => set('deliveryInfo', e.target.value)} placeholder="배송 안내" fullWidth />
              <Input label="SEO 제목" value={form.seoTitle} onChange={(e) => set('seoTitle', e.target.value)} placeholder="SEO 제목" fullWidth />
              <Input label="SEO 설명" value={form.seoDescription} onChange={(e) => set('seoDescription', e.target.value)} placeholder="SEO 설명" fullWidth />
              <Input label="SEO 키워드" value={form.seoKeywords} onChange={(e) => set('seoKeywords', e.target.value)} placeholder="SEO 키워드" fullWidth />
            </section>

            <ProductImageUpload imageUrl={form.imageUrl} onImageUrlChange={(url) => set('imageUrl', url)} />

            <section>
              <label className="block text-sm font-medium text-[#444] mb-1">상품 설명</label>
              <textarea value={form.description} onChange={(e) => set('description', e.target.value)} placeholder="상품 상세 설명 입력" rows={5} className="w-full border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] resize-none" />
            </section>

            <section className="space-y-4">
              <div className="flex items-center justify-between pb-3 border-b border-[#f0f1f5]">
                <h2 className="text-sm font-bold text-[#1a1f2e]">상품 옵션</h2>
                <Button variant="outline" size="sm" type="button" onClick={addOptionGroup}>옵션 그룹 추가</Button>
              </div>
              {optionGroups.length === 0 ? (
                <p className="py-5 text-sm text-[#8a9bb5]">색상, 사이즈처럼 구매자가 선택할 옵션이 있으면 추가하세요.</p>
              ) : optionGroups.map((og, idx) => (
                <div key={idx} className="border border-[#e8eaf0] p-4 space-y-3">
                  <div className="flex items-end gap-3">
                    <Input label="옵션명" value={og.name} onChange={(e) => updateOptionGroupName(idx, e.target.value)} placeholder="예: 색상" fullWidth />
                    <Button variant="ghost" size="sm" type="button" onClick={() => removeOptionGroup(idx)} className="mb-0.5 shrink-0 text-[#d94f4f]">삭제</Button>
                  </div>
                  <div className="flex gap-2">
                    <input value={og.valueInput} onChange={(e) => updateOptionGroupValueInput(idx, e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addOptionValue(idx); } }} placeholder="예: 블랙" className="flex-1 border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222]" />
                    <Button variant="outline" type="button" onClick={() => addOptionValue(idx)}>추가</Button>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {og.values.map((value) => (
                      <button key={value} type="button" onClick={() => removeOptionValue(idx, value)} className="border border-[#d8dce6] bg-[#f7f8fc] px-3 py-1 text-xs text-[#4f5b70] hover:border-[#d94f4f] hover:text-[#d94f4f]">
                        {value} ×
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </section>
          </div>

          <div className="flex items-center justify-end gap-3 mt-5">
            <Link href="/admin/products"><Button variant="outline" type="button">취소</Button></Link>
            <Button variant="primary" type="submit" disabled={submitting}>{submitting ? '등록 중...' : '상품 등록'}</Button>
          </div>
        </form>
      </div>
    </AdminLayout>
  );
}
