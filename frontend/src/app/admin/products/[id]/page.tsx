'use client';

import { use, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import ProductImageUpload from '@/components/admin/ProductImageUpload';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import {
  productService,
  type ApiAdminProductDetail,
  type ApiCategory,
  type ProductOperationNote,
  type ProductStatusHistory,
  type ProductDetailBlock,
  type ProductDetailBlockType,
  type ProductDisplayStatus,
  type ProductOptionGroup,
  type ProductSalesStatus,
} from '@/lib/services/productService';
import {
  formatDateTime,
  PRODUCT_DISPLAY_STATUS_LABEL,
  PRODUCT_SALES_STATUS_LABEL,
} from '@/lib/format';

interface OptionGroupDraft {
  name: string;
  valueInput: string;
  values: string[];
}

const DETAIL_BLOCK_TYPE_LABEL: Record<ProductDetailBlockType, string> = {
  HEADING: '제목',
  TEXT: '텍스트',
  IMAGE: '이미지',
  NOTICE: '안내 박스',
  SPEC_TABLE: '스펙표',
  HTML: 'HTML',
};

const optionalNumber = (value: string) => value.trim() ? Number(value) : undefined;
const optionalDateTime = (value: string) => value.trim() ? value : undefined;

export default function AdminProductEditPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const productId = Number(id);
  const [categories, setCategories] = useState<ApiCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [savingBlocks, setSavingBlocks] = useState(false);
  const [blockMessage, setBlockMessage] = useState('');
  const [operationNotes, setOperationNotes] = useState<ProductOperationNote[]>([]);
  const [statusHistories, setStatusHistories] = useState<ProductStatusHistory[]>([]);
  const [noteContent, setNoteContent] = useState('');
  const [savingNote, setSavingNote] = useState(false);
  const [noteMessage, setNoteMessage] = useState('');
  const [notFound, setNotFound] = useState(false);
  const [optionGroups, setOptionGroups] = useState<OptionGroupDraft[]>([]);
  const [detailBlocks, setDetailBlocks] = useState<ProductDetailBlock[]>([]);
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
    Promise.all([
      productService.getAdminProduct(productId),
      productService.getAdminProductDetailBlocks(productId),
      productService.getProductOperationNotes(productId),
      productService.getProductStatusHistory(productId),
      productService.getCategories(),
    ])
      .then(([product, blocks, notes, histories, cats]: [
        ApiAdminProductDetail,
        ProductDetailBlock[],
        ProductOperationNote[],
        ProductStatusHistory[],
        ApiCategory[],
      ]) => {
        setCategories(cats);
        setOperationNotes(notes ?? []);
        setStatusHistories(histories ?? []);
        setDetailBlocks((blocks ?? []).map((block, index) => ({
          ...block,
          sortOrder: block.sortOrder ?? index,
          visible: block.visible ?? true,
        })));
        setForm({
          categoryId: String(product.categoryId),
          name: product.name,
          productCode: product.productCode ?? '',
          brand: product.brand ?? '',
          manufacturer: product.manufacturer ?? '',
          modelName: product.modelName ?? '',
          origin: product.origin ?? '',
          price: String(product.price),
          originalPrice: product.originalPrice != null ? String(product.originalPrice) : '',
          discountPrice: product.discountPrice != null ? String(product.discountPrice) : '',
          purchasePrice: product.purchasePrice != null ? String(product.purchasePrice) : '',
          stockQuantity: String(product.stockQuantity),
          safetyStockQuantity: product.safetyStockQuantity != null ? String(product.safetyStockQuantity) : '5',
          searchKeywords: product.searchKeywords ?? '',
          tags: product.tags ?? '',
          saleStartAt: product.saleStartAt ? product.saleStartAt.slice(0, 16) : '',
          saleEndAt: product.saleEndAt ? product.saleEndAt.slice(0, 16) : '',
          deliveryInfo: product.deliveryInfo ?? '',
          seoTitle: product.seoTitle ?? '',
          seoDescription: product.seoDescription ?? '',
          seoKeywords: product.seoKeywords ?? '',
          description: product.description ?? '',
          imageUrl: product.imageUrl ?? '',
          status: product.status,
          salesStatus: product.salesStatus ?? 'ON_SALE',
          displayStatus: product.displayStatus ?? 'VISIBLE',
        });
        setOptionGroups((product.options ?? []).map((option) => ({ name: option.name, valueInput: '', values: option.values })));
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [productId]);

  const set = (field: string, value: string) => setForm((prev) => ({ ...prev, [field]: value }));

  const addOptionGroup = () => setOptionGroups((prev) => [...prev, { name: '', valueInput: '', values: [] }]);
  const removeOptionGroup = (idx: number) => setOptionGroups((prev) => prev.filter((_, i) => i !== idx));
  const updateOptionGroupName = (idx: number, name: string) => setOptionGroups((prev) => prev.map((og, i) => i === idx ? { ...og, name } : og));
  const updateOptionGroupValueInput = (idx: number, valueInput: string) => setOptionGroups((prev) => prev.map((og, i) => i === idx ? { ...og, valueInput } : og));
  const addOptionValue = (idx: number) => {
    setOptionGroups((prev) => prev.map((og, i) => {
      if (i !== idx) return og;
      const value = og.valueInput.trim();
      if (!value || og.values.includes(value)) return { ...og, valueInput: '' };
      return { ...og, values: [...og.values, value], valueInput: '' };
    }));
  };
  const removeOptionValue = (idx: number, value: string) => setOptionGroups((prev) => prev.map((og, i) => i === idx ? { ...og, values: og.values.filter((v) => v !== value) } : og));

  const addDetailBlock = (blockType: ProductDetailBlockType = 'TEXT') => setDetailBlocks((prev) => [...prev, {
    blockType,
    title: '',
    content: '',
    imageUrl: '',
    specJson: '',
    sortOrder: prev.length,
    visible: true,
  }]);
  const updateDetailBlock = (idx: number, patch: Partial<ProductDetailBlock>) => setDetailBlocks((prev) => prev.map((block, i) => i === idx ? { ...block, ...patch } : block));
  const removeDetailBlock = (idx: number) => setDetailBlocks((prev) => prev.filter((_, i) => i !== idx).map((block, i) => ({ ...block, sortOrder: i })));
  const moveDetailBlock = (idx: number, direction: -1 | 1) => {
    setDetailBlocks((prev) => {
      const nextIndex = idx + direction;
      if (nextIndex < 0 || nextIndex >= prev.length) return prev;
      const next = [...prev];
      [next[idx], next[nextIndex]] = [next[nextIndex], next[idx]];
      return next.map((block, i) => ({ ...block, sortOrder: i }));
    });
  };

  const handleSaveDetailBlocks = async () => {
    setSavingBlocks(true);
    setBlockMessage('');
    try {
      const saved = await productService.saveAdminProductDetailBlocks(productId, detailBlocks.map((block, index) => ({
        ...block,
        title: block.title?.trim() || undefined,
        content: block.content?.trim() || undefined,
        imageUrl: block.imageUrl?.trim() || undefined,
        specJson: block.specJson?.trim() || undefined,
        sortOrder: index,
      })));
      setDetailBlocks(saved.map((block, index) => ({ ...block, sortOrder: index })));
      setBlockMessage('상세 블록이 저장되었습니다.');
    } catch (err) {
      setBlockMessage(err instanceof Error ? err.message : '상세 블록 저장에 실패했습니다.');
    } finally {
      setSavingBlocks(false);
    }
  };

  const handleCreateNote = async () => {
    const content = noteContent.trim();
    if (!content) {
      setNoteMessage('운영 메모 내용을 입력해주세요.');
      return;
    }

    setSavingNote(true);
    setNoteMessage('');
    try {
      const saved = await productService.createProductOperationNote(productId, content);
      setOperationNotes((prev) => [saved, ...prev]);
      setNoteContent('');
      setNoteMessage('운영 메모가 저장되었습니다.');
    } catch (err) {
      setNoteMessage(err instanceof Error ? err.message : '운영 메모 저장에 실패했습니다.');
    } finally {
      setSavingNote(false);
    }
  };

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
      await productService.updateProduct(productId, {
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
        stockQuantity: Number(form.stockQuantity),
        safetyStockQuantity: optionalNumber(form.safetyStockQuantity),
        imageUrl: form.imageUrl.trim() || undefined,
        status: form.status,
        salesStatus: form.salesStatus as ProductSalesStatus,
        displayStatus: form.displayStatus as ProductDisplayStatus,
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
    return <AdminLayout title="상품 수정"><div className="py-12 text-center text-[#bbb] text-sm">불러오는 중...</div></AdminLayout>;
  }

  if (notFound) {
    return (
      <AdminLayout title="상품 수정">
        <div className="py-12 text-center text-[#bbb] text-sm">
          상품을 찾을 수 없습니다.<br />
          <Link href="/admin/products" className="text-[#1a1f2e] underline mt-2 inline-block">목록으로</Link>
        </div>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout title="상품 수정">
      <div className="max-w-[760px]">
        <Link href="/admin/products" className="text-sm text-[#8a9bb5] hover:text-[#1a1f2e] transition-colors">← 상품 목록</Link>

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
                <div>
                  <h2 className="text-sm font-bold text-[#1a1f2e]">상세페이지 블록</h2>
                  <p className="mt-1 text-xs text-[#8a9bb5]">노출 상태인 블록만 사용자 상품 상세에 순서대로 표시됩니다.</p>
                </div>
                <Button variant="outline" size="sm" type="button" onClick={() => addDetailBlock()}>블록 추가</Button>
              </div>
              {blockMessage && <p className="border border-[#e8eaf0] bg-[#f7f8fc] px-3 py-2 text-xs text-[#4f5b70]">{blockMessage}</p>}
              {detailBlocks.length === 0 ? (
                <div className="border border-dashed border-[#d8dce6] bg-[#fafbfe] px-4 py-6 text-center text-sm text-[#8a9bb5]">등록된 상세 블록이 없습니다.</div>
              ) : detailBlocks.map((block, idx) => (
                <div key={`${block.id ?? 'new'}-${idx}`} className="border border-[#e8eaf0] p-4 space-y-3">
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-sm font-medium text-[#444] mb-1">블록 유형</label>
                      <select value={block.blockType} onChange={(e) => updateDetailBlock(idx, { blockType: e.target.value as ProductDetailBlockType })} className="border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] w-full">
                        {(['HEADING', 'TEXT', 'IMAGE', 'NOTICE', 'SPEC_TABLE', 'HTML'] as ProductDetailBlockType[]).map((type) => <option key={type} value={type}>{DETAIL_BLOCK_TYPE_LABEL[type]}</option>)}
                      </select>
                    </div>
                    <Input label="제목" value={block.title ?? ''} onChange={(e) => updateDetailBlock(idx, { title: e.target.value })} placeholder="선택 입력" fullWidth />
                  </div>
                  {(block.blockType === 'TEXT' || block.blockType === 'NOTICE' || block.blockType === 'HTML' || block.blockType === 'HEADING') && (
                    <textarea value={block.content ?? ''} onChange={(e) => updateDetailBlock(idx, { content: e.target.value })} rows={block.blockType === 'HTML' ? 6 : 4} className="w-full border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] resize-none" />
                  )}
                  {block.blockType === 'IMAGE' && <Input label="이미지 URL" value={block.imageUrl ?? ''} onChange={(e) => updateDetailBlock(idx, { imageUrl: e.target.value })} placeholder="https://..." fullWidth />}
                  {block.blockType === 'SPEC_TABLE' && (
                    <textarea value={block.specJson ?? ''} onChange={(e) => updateDetailBlock(idx, { specJson: e.target.value })} placeholder='[{"label":"소재","value":"면"}]' rows={5} className="w-full border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] resize-none font-mono" />
                  )}
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <label className="inline-flex items-center gap-2 text-sm text-[#555]"><input type="checkbox" checked={block.visible} onChange={(e) => updateDetailBlock(idx, { visible: e.target.checked })} className="accent-[#222]" />노출</label>
                    <div className="flex gap-2">
                      <Button variant="ghost" size="sm" type="button" onClick={() => moveDetailBlock(idx, -1)} disabled={idx === 0}>위로</Button>
                      <Button variant="ghost" size="sm" type="button" onClick={() => moveDetailBlock(idx, 1)} disabled={idx === detailBlocks.length - 1}>아래로</Button>
                      <Button variant="danger" size="sm" type="button" onClick={() => removeDetailBlock(idx)}>삭제</Button>
                    </div>
                  </div>
                </div>
              ))}
              <div className="flex justify-end"><Button variant="secondary" type="button" disabled={savingBlocks} onClick={handleSaveDetailBlocks}>{savingBlocks ? '저장 중...' : '상세 블록 저장'}</Button></div>
            </section>

            <section className="space-y-4">
              <div className="pb-3 border-b border-[#f0f1f5]">
                <h2 className="text-sm font-bold text-[#1a1f2e]">상품 운영 메모</h2>
                <p className="mt-1 text-xs text-[#8a9bb5]">관리자 상품 운영 중 남겨야 할 사유와 참고사항을 기록합니다.</p>
              </div>
              <textarea
                value={noteContent}
                onChange={(e) => setNoteContent(e.target.value)}
                placeholder="운영 메모 입력"
                rows={4}
                className="w-full border border-[#ddd] bg-white text-[#222] text-sm px-3 py-2.5 outline-none focus:border-[#222] resize-none"
              />
              <div className="flex items-center justify-between gap-3">
                {noteMessage ? <p className="text-xs text-[#4f5b70]">{noteMessage}</p> : <span />}
                <Button variant="secondary" type="button" disabled={savingNote} onClick={handleCreateNote}>
                  {savingNote ? '저장 중...' : '운영 메모 저장'}
                </Button>
              </div>
              {operationNotes.length === 0 ? (
                <div className="border border-dashed border-[#d8dce6] bg-[#fafbfe] px-4 py-6 text-center text-sm text-[#8a9bb5]">등록된 운영 메모가 없습니다.</div>
              ) : (
                <div className="space-y-2">
                  {operationNotes.map((note) => (
                    <div key={note.id} className="border border-[#e8eaf0] bg-[#fafbfe] p-3">
                      <p className="whitespace-pre-wrap text-sm text-[#333]">{note.content}</p>
                      <p className="mt-2 text-xs text-[#8a9bb5]">{note.writerEmail || '-'} · {formatDateTime(note.createdAt)}</p>
                    </div>
                  ))}
                </div>
              )}
            </section>

            <section className="space-y-4">
              <div className="pb-3 border-b border-[#f0f1f5]">
                <h2 className="text-sm font-bold text-[#1a1f2e]">상태 변경 이력</h2>
                <p className="mt-1 text-xs text-[#8a9bb5]">단건/대량 상태 변경으로 실제 판매 또는 전시 상태가 바뀐 기록만 표시합니다.</p>
              </div>
              {statusHistories.length === 0 ? (
                <div className="border border-dashed border-[#d8dce6] bg-[#fafbfe] px-4 py-6 text-center text-sm text-[#8a9bb5]">상태 변경 이력이 없습니다.</div>
              ) : (
                <div className="overflow-x-auto border border-[#e8eaf0]">
                  <table className="w-full text-sm">
                    <thead className="bg-[#f8f9fb] text-xs text-[#8a9bb5]">
                      <tr>
                        <th className="px-3 py-2 text-left">변경일시</th>
                        <th className="px-3 py-2 text-left">작업자</th>
                        <th className="px-3 py-2 text-left">판매 상태</th>
                        <th className="px-3 py-2 text-left">전시 상태</th>
                        <th className="px-3 py-2 text-left">사유</th>
                      </tr>
                    </thead>
                    <tbody>
                      {statusHistories.map((history) => (
                        <tr key={history.id} className="border-t border-[#f0f1f5]">
                          <td className="px-3 py-2 whitespace-nowrap">{formatDateTime(history.createdAt)}</td>
                          <td className="px-3 py-2 whitespace-nowrap">{history.changedByEmail || '-'}</td>
                          <td className="px-3 py-2 whitespace-nowrap">
                            {history.previousSalesStatus ? PRODUCT_SALES_STATUS_LABEL[history.previousSalesStatus] : '-'}
                            {' → '}
                            {history.newSalesStatus ? PRODUCT_SALES_STATUS_LABEL[history.newSalesStatus] : '-'}
                          </td>
                          <td className="px-3 py-2 whitespace-nowrap">
                            {history.previousDisplayStatus ? PRODUCT_DISPLAY_STATUS_LABEL[history.previousDisplayStatus] : '-'}
                            {' → '}
                            {history.newDisplayStatus ? PRODUCT_DISPLAY_STATUS_LABEL[history.newDisplayStatus] : '-'}
                          </td>
                          <td className="px-3 py-2">{history.reason || '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
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
                    {og.values.map((value) => <button key={value} type="button" onClick={() => removeOptionValue(idx, value)} className="border border-[#d8dce6] bg-[#f7f8fc] px-3 py-1 text-xs text-[#4f5b70] hover:border-[#d94f4f] hover:text-[#d94f4f]">{value} ×</button>)}
                  </div>
                </div>
              ))}
            </section>
          </div>

          <div className="flex items-center justify-end gap-3 mt-5">
            <Link href="/admin/products"><Button variant="outline" type="button">취소</Button></Link>
            <Button variant="primary" type="submit" disabled={submitting}>{submitting ? '저장 중...' : '수정 완료'}</Button>
          </div>
        </form>
      </div>
    </AdminLayout>
  );
}
