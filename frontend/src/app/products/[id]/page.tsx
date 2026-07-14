'use client';

import { use, useState, useEffect } from 'react';
import Image from 'next/image';
import DOMPurify from 'isomorphic-dompurify';
import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import Button from '@/components/common/Button';
import { productService, type ApiProductDetail, type ProductDetailBlock } from '@/lib/services/productService';
import { cartService } from '@/lib/services/cartService';
import { inquiryService, type ApiInquiry } from '@/lib/services/inquiryService';
import { reviewService, type ApiReview } from '@/lib/services/reviewService';
import { wishlistService } from '@/lib/services/wishlistService';
import { formatPrice, formatDateTime, INQUIRY_STATUS_LABEL, INQUIRY_STATUS_COLOR, PRODUCT_SALES_STATUS_LABEL } from '@/lib/format';
import { notifyCartChanged } from '@/contexts/CartContext';
import { useRouter } from 'next/navigation';

function parseSpecRows(specJson?: string | null): { label: string; value: string }[] {
  if (!specJson) return [];
  try {
    const parsed = JSON.parse(specJson);
    if (!Array.isArray(parsed)) return [];
    return parsed
      .map((row) => ({ label: String(row?.label ?? ''), value: String(row?.value ?? '') }))
      .filter((row) => row.label || row.value);
  } catch {
    return [];
  }
}

function ProductDetailBlockView({ block }: { block: ProductDetailBlock }) {
  if (block.blockType === 'HEADING') {
    return <section><h2 className="text-xl font-bold text-[#222] mb-3">{block.title || block.content}</h2></section>;
  }
  if (block.blockType === 'TEXT') {
    return <section>{block.title && <h3 className="text-base font-bold text-[#222] mb-2">{block.title}</h3>}<p className="whitespace-pre-wrap text-[#555]">{block.content}</p></section>;
  }
  if (block.blockType === 'IMAGE') {
    const src = block.imageUrl || 'https://placehold.co/900x560?text=No+Image';
    return <section>{block.title && <h3 className="text-base font-bold text-[#222] mb-3">{block.title}</h3>}<div className="relative aspect-[16/10] bg-[#f7f7f7]"><Image src={src} alt={block.title || '상품 상세 이미지'} fill className="object-cover" sizes="100vw" /></div></section>;
  }
  if (block.blockType === 'NOTICE') {
    return <section className="border border-[#e8eaf0] bg-[#f7f8fc] px-4 py-3">{block.title && <p className="font-bold text-[#333] mb-1">{block.title}</p>}<p className="whitespace-pre-wrap text-[#555]">{block.content}</p></section>;
  }
  if (block.blockType === 'SPEC_TABLE') {
    const rows = parseSpecRows(block.specJson);
    if (rows.length === 0) return null;
    return <section>{block.title && <h3 className="text-base font-bold text-[#222] mb-3">{block.title}</h3>}<div className="border border-[#e5e5e5] divide-y divide-[#e5e5e5]">{rows.map((row, index) => <div key={`${row.label}-${index}`} className="grid grid-cols-[140px_1fr] text-sm"><div className="bg-[#fafafa] px-4 py-3 text-[#777]">{row.label}</div><div className="px-4 py-3 text-[#444]">{row.value}</div></div>)}</div></section>;
  }
  if (block.blockType === 'HTML') {
    const sanitizedHtml = DOMPurify.sanitize(block.content || '', { ALLOWED_TAGS: ['p', 'br', 'strong', 'b', 'em', 'i', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'blockquote', 'a', 'img'], ALLOWED_ATTR: ['href', 'src', 'alt', 'width', 'height'], ALLOW_DATA_ATTR: false, FORBID_TAGS: ['script', 'iframe', 'object', 'embed', 'form', 'style', 'svg', 'math'], FORBID_ATTR: ['style'] });
    return <section>{block.title && <h3 className="text-base font-bold text-[#222] mb-2">{block.title}</h3>}<div className="prose max-w-none" dangerouslySetInnerHTML={{ __html: sanitizedHtml }} /></section>;
  }
  return null;
}

const splitTags = (tags?: string | null) => (tags ?? '').split(',').map((tag) => tag.trim()).filter(Boolean).slice(0, 5);

function getPurchaseUnavailableReason(product: ApiProductDetail): string {
  if (product.stockDisplayStatus === 'SOLD_OUT' || product.stockQuantity <= 0) return '품절된 상품입니다.';
  if (product.salesStatus === 'PAUSED') return '일시 판매 중지된 상품입니다.';
  if (product.salesStatus === 'DISCONTINUED') return '판매 종료된 상품입니다.';
  if (product.salesStatus === 'DRAFT') return '아직 판매 준비 중인 상품입니다.';
  if (product.salesStatus === 'SOLD_OUT') return '품절 처리된 상품입니다.';
  return '현재 구매할 수 없는 상품입니다.';
}

export default function ProductDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const router = useRouter();
  const { id } = use(params);
  const [product, setProduct] = useState<ApiProductDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [activeTab, setActiveTab] = useState<'detail' | 'shipping' | 'review' | 'inquiry'>('detail');
  const [addingToCart, setAddingToCart] = useState(false);
  const [inquiries, setInquiries] = useState<ApiInquiry[]>([]);
  const [inquirySubject, setInquirySubject] = useState('');
  const [inquiryContent, setInquiryContent] = useState('');
  const [submittingInquiry, setSubmittingInquiry] = useState(false);
  const [reviews, setReviews] = useState<ApiReview[]>([]);
  const [reviewTotalElements, setReviewTotalElements] = useState(0);
  const [liked, setLiked] = useState(false);
  const [wishlistLoading, setWishlistLoading] = useState(false);
  const [selectedOptions, setSelectedOptions] = useState<Record<string, string>>({});
  const [actionMessage, setActionMessage] = useState('');
  const [tabError, setTabError] = useState('');

  useEffect(() => {
    productService
      .getProduct(Number(id))
      .then((p) => {
        setProduct(p);
        setLoading(false);
        try {
          const raw = localStorage.getItem('recent_products');
          const prev: { id: number; name: string; price: number; imageUrl: string; categoryName: string }[] = raw ? JSON.parse(raw) : [];
          const filtered = prev.filter((x) => x.id !== p.id);
          const updated = [{ id: p.id, name: p.name, price: p.price, imageUrl: p.imageUrl ?? 'https://placehold.co/600x750?text=No+Image', categoryName: p.categoryName }, ...filtered].slice(0, 10);
          localStorage.setItem('recent_products', JSON.stringify(updated));
        } catch { /* ignore */ }
      })
      .catch(() => {
        setNotFound(true);
        setLoading(false);
      });

    const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
    if (token) wishlistService.getStatus(Number(id)).then((res) => setLiked(res.liked)).catch(() => {});
  }, [id]);

  useEffect(() => {
    if (activeTab === 'inquiry') {
      inquiryService.getProductInquiries(Number(id)).then(setInquiries).catch(() => {
        setInquiries([]);
        setTabError('문의 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
      });
    }
    if (activeTab === 'review') {
      reviewService.getProductReviews(Number(id), 0, 20).then((res) => {
        setReviews(res.content);
        setReviewTotalElements(res.totalElements);
      }).catch(() => {
        setReviews([]);
        setReviewTotalElements(0);
        setTabError('리뷰 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
      });
    }
  }, [activeTab, id]);

  const handleInquirySubmit = async () => {
    if (!inquirySubject.trim() || !inquiryContent.trim()) {
      setActionMessage('제목과 내용을 모두 입력하세요.');
      return;
    }
    setSubmittingInquiry(true);
    try {
      await inquiryService.createProductInquiry(Number(id), 'PRODUCT', inquirySubject.trim(), inquiryContent.trim());
      setInquirySubject('');
      setInquiryContent('');
      inquiryService.getProductInquiries(Number(id)).then(setInquiries).catch(() => {});
      setActionMessage('문의가 등록되었습니다.');
    } catch (err) {
      setActionMessage(err instanceof Error ? err.message : '문의 등록에 실패했습니다.');
    } finally {
      setSubmittingInquiry(false);
    }
  };

  const handleToggleWishlist = async () => {
    setWishlistLoading(true);
    try {
      const res = await wishlistService.toggle(Number(id));
      setLiked(res.liked);
      setActionMessage(res.liked ? '찜 목록에 추가되었습니다.' : '찜 목록에서 제거되었습니다.');
    } catch {
      setActionMessage('로그인이 필요한 서비스입니다.');
    } finally {
      setWishlistLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!product) return;
    if (!product.purchasable) {
      setActionMessage(getPurchaseUnavailableReason(product));
      return;
    }
    if (product.options?.length > 0) {
      const missing = product.options.find((og) => !selectedOptions[og.name]);
      if (missing) {
        setActionMessage(`"${missing.name}" 옵션을 선택해주세요.`);
        return;
      }
    }
    try {
      setAddingToCart(true);
      const opts = Object.keys(selectedOptions).length > 0 ? selectedOptions : undefined;
      await cartService.addToCart(product.id, quantity, opts);
      notifyCartChanged();
      setActionMessage(`"${product.name}" 상품을 장바구니에 담았습니다.`);
    } catch (err) {
      setActionMessage(err instanceof Error ? err.message : '장바구니 추가에 실패했습니다. 상품 상태를 확인해주세요.');
    } finally {
      setAddingToCart(false);
    }
  };

  const handleBuyNow = () => {
    if (!product || !product.purchasable || quantity < 1 || quantity > product.stockQuantity) { setActionMessage('구매 수량과 재고를 확인해주세요.'); return; }
    const missing = product.options?.find((group) => !selectedOptions[group.name]);
    if (missing) { setActionMessage(`"${missing.name}" 옵션을 선택해주세요.`); return; }
    sessionStorage.setItem('buy_now_item', JSON.stringify({ productId: product.id, quantity, selectedOptions }));
    router.push('/orders/checkout?mode=buy-now');
  };

  const handleTabChange = (tab: 'detail' | 'shipping' | 'review' | 'inquiry') => {
    setTabError('');
    setActiveTab(tab);
  };

  if (loading) {
    return <><ShopHeader /><div className="max-w-[1200px] mx-auto px-4 py-20 text-center text-[#aaa]">상품을 불러오는 중...</div><ShopFooter /></>;
  }

  if (notFound || !product) {
    return <><ShopHeader /><div className="max-w-[1200px] mx-auto px-4 py-20 text-center text-[#aaa]">상품을 찾을 수 없습니다.<br /><Link href="/products" className="text-[#222] underline mt-4 inline-block">목록으로 돌아가기</Link></div><ShopFooter /></>;
  }

  const isPurchasable = Boolean(product.purchasable);
  const unavailableReason = isPurchasable ? '' : getPurchaseUnavailableReason(product);
  const stockLabel = product.stockDisplayText ?? (product.stockQuantity > 0 ? '구매 가능' : '품절');
  const imageSrc = product.imageUrl || 'https://placehold.co/600x750?text=No+Image';
  const tags = splitTags(product.tags);
  const discountAmount = product.originalPrice && product.originalPrice > product.price ? product.originalPrice - product.price : 0;

  return (
    <>
      <ShopHeader />

      <main className="max-w-[1200px] mx-auto px-4 py-10">
        <div className="flex items-center gap-2 text-xs text-[#999] mb-8">
          <Link href="/" className="hover:text-[#222]">홈</Link><span>/</span><Link href="/products" className="hover:text-[#222]">상품 목록</Link><span>/</span><span className="text-[#444]">{product.categoryName}</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 mb-16">
          <div className="relative aspect-[4/5] bg-[#f7f7f7]">
            <Image src={imageSrc} alt={product.name} fill className="object-cover" sizes="(max-width: 768px) 100vw, 50vw" priority />
            {!isPurchasable && <div className="absolute inset-0 bg-white/60 flex items-center justify-center"><span className="bg-[#777] text-white text-sm font-medium px-6 py-2 tracking-widest">{stockLabel}</span></div>}
          </div>

          <div className="flex flex-col">
            <p className="text-xs text-[#999] tracking-wide mb-2">{product.brand || product.categoryName}</p>
            <h1 className="text-2xl font-bold text-[#222] leading-snug mb-4">{product.name}</h1>
            {tags.length > 0 && <div className="flex flex-wrap gap-1 mb-4">{tags.map((tag) => <span key={tag} className="bg-[#f7f8fc] text-[#7b8494] text-xs px-2 py-1">#{tag}</span>)}</div>}

            <div className="flex items-baseline gap-3 mb-2">
              <span className="text-2xl font-bold text-[#222]">{formatPrice(product.price)}</span>
              {product.originalPrice && product.originalPrice > product.price && <span className="text-sm text-[#bbb] line-through">{formatPrice(product.originalPrice)}</span>}
            </div>
            {discountAmount > 0 && <p className="text-sm text-[#d94f4f] mb-4">{formatPrice(discountAmount)} 할인</p>}

            <div className="border-t border-[#f0f0f0] pt-6 space-y-3 text-sm">
              <InfoRow label="재고 상태" value={stockLabel} emphasize={!isPurchasable} />
              <InfoRow label="판매 상태" value={PRODUCT_SALES_STATUS_LABEL[product.salesStatus] ?? product.salesStatus} />
              {product.manufacturer && <InfoRow label="제조사" value={product.manufacturer} />}
              {product.origin && <InfoRow label="원산지" value={product.origin} />}
              {product.deliveryInfo && <InfoRow label="배송 정보" value={product.deliveryInfo} />}
              {!isPurchasable && <p className="border border-[#f0d6d6] bg-[#fff7f7] px-3 py-2 text-xs text-[#c43a3a]">{unavailableReason}</p>}

              {product.options?.length > 0 && product.options.map((og) => (
                <div key={og.name} className="flex items-center gap-4">
                  <span className="w-20 text-[#999] shrink-0">{og.name}</span>
                  <select value={selectedOptions[og.name] ?? ''} onChange={(e) => setSelectedOptions((prev) => ({ ...prev, [og.name]: e.target.value }))} className="flex-1 border border-[#ddd] px-3 py-2 text-sm text-[#333] outline-none focus:border-[#222] bg-white" disabled={!isPurchasable}>
                    <option value="">선택하세요</option>
                    {og.values.map((v) => <option key={v} value={v}>{v}</option>)}
                  </select>
                </div>
              ))}

              {isPurchasable && (
                <div className="flex items-center gap-4">
                  <span className="w-20 text-[#999]">수량</span>
                  <div className="flex items-center border border-[#ddd]">
                    <button onClick={() => setQuantity(Math.max(1, quantity - 1))} className="w-10 h-10 flex items-center justify-center text-[#555] hover:bg-[#f5f5f5] transition-colors">-</button>
                    <span className="w-12 text-center text-[#222] font-medium">{quantity}</span>
                    <button onClick={() => setQuantity(Math.min(product.stockQuantity, quantity + 1))} className="w-10 h-10 flex items-center justify-center text-[#555] hover:bg-[#f5f5f5] transition-colors">+</button>
                  </div>
                </div>
              )}
            </div>

            {isPurchasable && <div className="border-t border-[#f0f0f0] mt-6 pt-4 flex items-center justify-between"><span className="text-sm text-[#999]">합계</span><span className="text-xl font-bold text-[#222]">{formatPrice(product.price * quantity)}</span></div>}

            <div className="mt-6 flex flex-col gap-3">
              {actionMessage && <p className="border border-[#f0d6d6] bg-[#fff7f7] px-3 py-2 text-xs text-[#c43a3a]">{actionMessage}</p>}
              <div className="flex gap-3">
                <Button variant="secondary" size="lg" fullWidth disabled={!isPurchasable || addingToCart} onClick={handleAddToCart}>{!isPurchasable ? '구매 불가' : addingToCart ? '추가 중...' : '장바구니 담기'}</Button>
                <button onClick={handleToggleWishlist} disabled={wishlistLoading} className={['w-12 h-12 flex items-center justify-center border text-xl transition-colors shrink-0', liked ? 'border-[#e05252] text-[#e05252] bg-[#fff5f5]' : 'border-[#ddd] text-[#bbb] hover:border-[#e05252] hover:text-[#e05252]'].join(' ')} aria-label={liked ? '찜 해제' : '찜하기'}>{liked ? '♥' : '♡'}</button>
              </div>
              {isPurchasable && <Button variant="primary" size="lg" fullWidth onClick={handleBuyNow}>바로 구매하기</Button>}
            </div>
          </div>
        </div>

        <div>
          <div className="flex border-b border-[#e5e5e5] mb-8 overflow-x-auto">
            {([['detail', '상품 상세'], ['shipping', '배송/교환/반품'], ['review', `리뷰 ${reviewTotalElements > 0 ? `(${reviewTotalElements})` : ''}`], ['inquiry', '상품 문의']] as const).map(([tab, label]) => (
              <button key={tab} onClick={() => handleTabChange(tab)} className={['px-8 py-3.5 text-sm font-medium transition-colors whitespace-nowrap', activeTab === tab ? 'border-b-2 border-[#222] text-[#222]' : 'text-[#999] hover:text-[#555]'].join(' ')}>{label}</button>
            ))}
          </div>

          <div className="min-h-[200px] py-8 text-sm text-[#555] leading-relaxed">
            {tabError && <div className="mb-5 border border-[#f0d6d6] bg-[#fff7f7] px-4 py-3 text-sm text-[#c43a3a]">{tabError}</div>}
            {activeTab === 'detail' && <div className="max-w-[760px] space-y-8">{product.detailBlocks?.length > 0 ? product.detailBlocks.map((block, index) => <ProductDetailBlockView key={block.id ?? `${block.blockType}-${index}`} block={block} />) : <p className="whitespace-pre-wrap">{product.description || '등록된 상세 설명이 없습니다.'}</p>}</div>}
            {activeTab === 'shipping' && <div className="space-y-4 text-[#777]"><div><p className="font-medium text-[#333] mb-1">배송 안내</p><p>{product.deliveryInfo || '결제 완료 후 1~3 영업일 내 출고됩니다.'}</p></div><div><p className="font-medium text-[#333] mb-1">교환/반품 안내</p><p>수령 후 7일 이내 교환/반품 신청이 가능합니다.</p><p>사용 흔적이나 상품 훼손이 있는 경우 교환/반품이 제한될 수 있습니다.</p></div></div>}
            {activeTab === 'review' && <ReviewList reviews={reviews} />}
            {activeTab === 'inquiry' && <InquiryPanel inquiries={inquiries} subject={inquirySubject} content={inquiryContent} submitting={submittingInquiry} onSubject={setInquirySubject} onContent={setInquiryContent} onSubmit={handleInquirySubmit} />}
          </div>
        </div>
      </main>

      <ShopFooter />
    </>
  );
}

function InfoRow({ label, value, emphasize = false }: { label: string; value: string; emphasize?: boolean }) {
  return <div className="flex gap-4"><span className="w-20 text-[#999] shrink-0">{label}</span><span className={emphasize ? 'text-[#d94f4f] font-medium' : 'text-[#333]'}>{value}</span></div>;
}

function ReviewList({ reviews }: { reviews: ApiReview[] }) {
  if (reviews.length === 0) return <div className="text-center text-[#bbb] py-8 text-sm">아직 리뷰가 없습니다.</div>;
  const avg = reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length;
  return <div><div className="flex items-center gap-3 mb-6 pb-4 border-b border-[#f0f0f0]"><span className="text-3xl font-bold text-[#222]">{avg.toFixed(1)}</span><div><div className="text-yellow-400 text-lg tracking-wide">{'★'.repeat(Math.round(avg))}{'☆'.repeat(5 - Math.round(avg))}</div><p className="text-xs text-[#999] mt-0.5">리뷰 {reviews.length}개</p></div></div><div className="divide-y divide-[#f0f0f0]">{reviews.map((review) => <div key={review.reviewId} className="py-4"><div className="flex items-center gap-3 mb-1"><span className="text-yellow-400 text-sm tracking-wide">{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</span><span className="text-xs font-medium text-[#333]">{review.userName}</span><span className="text-xs text-[#bbb] ml-auto">{formatDateTime(review.createdAt)}</span></div>{review.content && <p className="text-sm text-[#555] mt-1 whitespace-pre-wrap">{review.content}</p>}</div>)}</div></div>;
}

function InquiryPanel({ inquiries, subject, content, submitting, onSubject, onContent, onSubmit }: { inquiries: ApiInquiry[]; subject: string; content: string; submitting: boolean; onSubject: (value: string) => void; onContent: (value: string) => void; onSubmit: () => void }) {
  return <div><div className="border border-[#e5e5e5] p-5 mb-6"><h3 className="text-sm font-bold text-[#222] mb-4">문의 작성</h3><div className="space-y-3"><input type="text" value={subject} onChange={(e) => onSubject(e.target.value)} placeholder="문의 제목을 입력하세요" className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#222]" /><textarea value={content} onChange={(e) => onContent(e.target.value)} placeholder="문의 내용을 입력하세요" rows={4} className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#222] resize-none" /><Button variant="primary" size="sm" disabled={submitting} onClick={onSubmit}>{submitting ? '등록 중...' : '문의 등록'}</Button></div></div>{inquiries.length === 0 ? <div className="text-center text-[#bbb] py-8 text-sm">등록된 문의가 없습니다.</div> : <div className="divide-y divide-[#f0f0f0]">{inquiries.map((inquiry) => <div key={inquiry.inquiryId} className="py-4"><div className="flex items-center gap-3 mb-1"><span className={`text-xs px-2 py-0.5 ${INQUIRY_STATUS_COLOR[inquiry.status] ?? ''}`}>{INQUIRY_STATUS_LABEL[inquiry.status] ?? inquiry.status}</span><span className="text-xs font-medium text-[#333]">{inquiry.subject}</span><span className="text-xs text-[#bbb] ml-auto">{inquiry.userName} · {formatDateTime(inquiry.createdAt)}</span></div><p className="text-xs text-[#777] mt-1 whitespace-pre-wrap">{inquiry.content}</p>{inquiry.answer && <div className="bg-[#f7f8fc] border-l-2 border-[#4c74e5] px-4 py-2 mt-2"><span className="text-xs font-bold text-[#4c74e5]">관리자 답변</span><p className="text-xs text-[#555] mt-0.5 whitespace-pre-wrap">{inquiry.answer}</p></div>}</div>)}</div>}</div>;
}
