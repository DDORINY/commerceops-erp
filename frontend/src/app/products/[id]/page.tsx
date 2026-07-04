'use client';

import { use, useState, useEffect } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import Button from '@/components/common/Button';
import { productService, type ApiProductDetail } from '@/lib/services/productService';
import { cartService } from '@/lib/services/cartService';
import { inquiryService, type ApiInquiry } from '@/lib/services/inquiryService';
import { reviewService, type ApiReview } from '@/lib/services/reviewService';
import { wishlistService } from '@/lib/services/wishlistService';
import { formatPrice, formatDateTime, INQUIRY_STATUS_LABEL, INQUIRY_STATUS_COLOR } from '@/lib/format';

export default function ProductDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
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

  useEffect(() => {
    productService
      .getProduct(Number(id))
      .then((p) => {
        setProduct(p);
        setLoading(false);
        // Save to recently viewed (max 10, newest first)
        try {
          const raw = localStorage.getItem('recent_products');
          const prev: { id: number; name: string; price: number; imageUrl: string; categoryName: string }[] = raw ? JSON.parse(raw) : [];
          const filtered = prev.filter((x) => x.id !== p.id);
          const updated = [{ id: p.id, name: p.name, price: p.price, imageUrl: p.imageUrl, categoryName: p.categoryName }, ...filtered].slice(0, 10);
          localStorage.setItem('recent_products', JSON.stringify(updated));
        } catch { /* ignore */ }
      })
      .catch(() => {
        setNotFound(true);
        setLoading(false);
      });

    // Check wishlist status
    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
    if (token) {
      wishlistService.getStatus(Number(id)).then((res) => setLiked(res.liked)).catch(() => {});
    }
  }, [id]);

  useEffect(() => {
    if (activeTab === 'inquiry') {
      inquiryService.getProductInquiries(Number(id)).then(setInquiries).catch(() => {});
    }
    if (activeTab === 'review') {
      reviewService.getProductReviews(Number(id), 0, 20).then((res) => {
        setReviews(res.content);
        setReviewTotalElements(res.totalElements);
      }).catch(() => {});
    }
  }, [activeTab, id]);

  const handleInquirySubmit = async () => {
    if (!inquirySubject.trim() || !inquiryContent.trim()) {
      alert('제목과 내용을 모두 입력하세요.');
      return;
    }
    setSubmittingInquiry(true);
    try {
      await inquiryService.createProductInquiry(Number(id), 'PRODUCT', inquirySubject.trim(), inquiryContent.trim());
      setInquirySubject('');
      setInquiryContent('');
      inquiryService.getProductInquiries(Number(id)).then(setInquiries).catch(() => {});
      alert('문의가 등록되었습니다.');
    } catch (err) {
      alert(err instanceof Error ? err.message : '문의 등록에 실패했습니다.');
    } finally {
      setSubmittingInquiry(false);
    }
  };

  const handleToggleWishlist = async () => {
    setWishlistLoading(true);
    try {
      const res = await wishlistService.toggle(Number(id));
      setLiked(res.liked);
    } catch {
      alert('로그인이 필요한 서비스입니다.');
    } finally {
      setWishlistLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!product) return;
    // 옵션이 있는 상품인데 선택되지 않은 옵션이 있으면 경고
    if (product.options?.length > 0) {
      const missing = product.options.find((og) => !selectedOptions[og.name]);
      if (missing) {
        alert(`"${missing.name}" 옵션을 선택해주세요.`);
        return;
      }
    }
    try {
      setAddingToCart(true);
      const opts = Object.keys(selectedOptions).length > 0 ? selectedOptions : undefined;
      await cartService.addToCart(product.id, quantity, opts);
      alert(`"${product.name}" 장바구니에 추가됐습니다.`);
    } catch (err) {
      alert(err instanceof Error ? err.message : '장바구니 추가에 실패했습니다.');
    } finally {
      setAddingToCart(false);
    }
  };

  if (loading) {
    return (
      <>
        <ShopHeader />
        <div className="max-w-[1200px] mx-auto px-4 py-20 text-center text-[#aaa]">
          상품을 불러오는 중...
        </div>
        <ShopFooter />
      </>
    );
  }

  if (notFound || !product) {
    return (
      <>
        <ShopHeader />
        <div className="max-w-[1200px] mx-auto px-4 py-20 text-center text-[#aaa]">
          상품을 찾을 수 없습니다.
          <br />
          <Link href="/products" className="text-[#222] underline mt-4 inline-block">
            목록으로 돌아가기
          </Link>
        </div>
        <ShopFooter />
      </>
    );
  }

  const isSoldOut = product.status === 'SOLD_OUT' || product.stockQuantity === 0;

  return (
    <>
      <ShopHeader />

      <main className="max-w-[1200px] mx-auto px-4 py-10">
        <div className="flex items-center gap-2 text-xs text-[#999] mb-8">
          <Link href="/" className="hover:text-[#222]">홈</Link>
          <span>/</span>
          <Link href="/products" className="hover:text-[#222]">상품 목록</Link>
          <span>/</span>
          <span className="text-[#444]">{product.categoryName}</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 mb-16">
          <div className="relative aspect-[4/5] bg-[#f7f7f7]">
            <Image
              src={product.imageUrl}
              alt={product.name}
              fill
              className="object-cover"
              sizes="(max-width: 768px) 100vw, 50vw"
              priority
            />
            {isSoldOut && (
              <div className="absolute inset-0 bg-white/60 flex items-center justify-center">
                <span className="bg-[#777] text-white text-sm font-medium px-6 py-2 tracking-widest">
                  SOLD OUT
                </span>
              </div>
            )}
          </div>

          <div className="flex flex-col">
            <p className="text-xs text-[#999] tracking-wide mb-2">{product.categoryName}</p>
            <h1 className="text-2xl font-bold text-[#222] leading-snug mb-4">{product.name}</h1>

            <div className="flex items-baseline gap-3 mb-1">
              <span className="text-2xl font-bold text-[#222]">
                {formatPrice(product.price)}
              </span>
            </div>

            <p className="text-sm text-[#999] mb-6">배송비 3,000원 (5만원 이상 무료)</p>

            <div className="border-t border-[#f0f0f0] pt-6 space-y-4">
              {/* 옵션 선택 */}
              {product.options?.length > 0 && product.options.map((og) => (
                <div key={og.name} className="flex items-center gap-4 text-sm">
                  <span className="w-20 text-[#999] shrink-0">{og.name}</span>
                  <select
                    value={selectedOptions[og.name] ?? ''}
                    onChange={(e) => setSelectedOptions((prev) => ({ ...prev, [og.name]: e.target.value }))}
                    className="flex-1 border border-[#ddd] px-3 py-2 text-sm text-[#333] outline-none focus:border-[#222] bg-white"
                  >
                    <option value="">선택하세요</option>
                    {og.values.map((v) => (
                      <option key={v} value={v}>{v}</option>
                    ))}
                  </select>
                </div>
              ))}

              <div className="flex items-center gap-4 text-sm">
                <span className="w-20 text-[#999]">재고 현황</span>
                <span className={isSoldOut ? 'text-[#d94f4f] font-medium' : 'text-[#222]'}>
                  {isSoldOut ? '품절' : `${product.stockQuantity}개 남음`}
                </span>
              </div>

              {!isSoldOut && (
                <div className="flex items-center gap-4 text-sm">
                  <span className="w-20 text-[#999]">수량</span>
                  <div className="flex items-center border border-[#ddd]">
                    <button
                      onClick={() => setQuantity(Math.max(1, quantity - 1))}
                      className="w-10 h-10 flex items-center justify-center text-[#555] hover:bg-[#f5f5f5] transition-colors"
                    >
                      −
                    </button>
                    <span className="w-12 text-center text-[#222] font-medium">{quantity}</span>
                    <button
                      onClick={() => setQuantity(Math.min(product.stockQuantity, quantity + 1))}
                      className="w-10 h-10 flex items-center justify-center text-[#555] hover:bg-[#f5f5f5] transition-colors"
                    >
                      +
                    </button>
                  </div>
                </div>
              )}
            </div>

            {!isSoldOut && (
              <div className="border-t border-[#f0f0f0] mt-6 pt-4 flex items-center justify-between">
                <span className="text-sm text-[#999]">합계</span>
                <span className="text-xl font-bold text-[#222]">
                  {formatPrice(product.price * quantity)}
                </span>
              </div>
            )}

            <div className="mt-6 flex flex-col gap-3">
              <div className="flex gap-3">
                <Button
                  variant="secondary"
                  size="lg"
                  fullWidth
                  disabled={isSoldOut || addingToCart}
                  onClick={handleAddToCart}
                >
                  {isSoldOut ? '품절된 상품입니다' : addingToCart ? '추가 중...' : '장바구니 담기'}
                </Button>
                <button
                  onClick={handleToggleWishlist}
                  disabled={wishlistLoading}
                  className={[
                    'w-12 h-12 flex items-center justify-center border text-xl transition-colors shrink-0',
                    liked ? 'border-[#e05252] text-[#e05252] bg-[#fff5f5]' : 'border-[#ddd] text-[#bbb] hover:border-[#e05252] hover:text-[#e05252]',
                  ].join(' ')}
                  aria-label={liked ? '찜 해제' : '찜하기'}
                >
                  {liked ? '♥' : '♡'}
                </button>
              </div>
              {!isSoldOut && (
                <Button variant="primary" size="lg" fullWidth>
                  바로 구매하기
                </Button>
              )}
            </div>
          </div>
        </div>

        <div>
          <div className="flex border-b border-[#e5e5e5] mb-8">
            {([['detail', '상품 상세'], ['shipping', '배송/교환/반품'], ['review', `리뷰 ${reviewTotalElements > 0 ? `(${reviewTotalElements})` : ''}`], ['inquiry', '상품 문의']] as const).map(
              ([tab, label]) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={[
                    'px-8 py-3.5 text-sm font-medium transition-colors',
                    activeTab === tab
                      ? 'border-b-2 border-[#222] text-[#222]'
                      : 'text-[#999] hover:text-[#555]',
                  ].join(' ')}
                >
                  {label}
                </button>
              )
            )}
          </div>

          <div className="min-h-[200px] py-8 text-sm text-[#555] leading-relaxed">
            {activeTab === 'detail' && (
              <div className="max-w-[600px]">
                <p className="mb-4">{product.description || `${product.name}은 편안한 착용감과 세련된 디자인을 자랑하는 제품입니다.`}</p>
                <ul className="space-y-2 text-[#777]">
                  <li>소재: 폴리에스터 65%, 면 35%</li>
                  <li>세탁 방법: 손세탁 권장, 30도 이하</li>
                  <li>원산지: 국내산</li>
                  <li>제조사: (주)커머스옵스 패션</li>
                </ul>
              </div>
            )}
            {activeTab === 'shipping' && (
              <div className="space-y-4 text-[#777]">
                <div>
                  <p className="font-medium text-[#333] mb-1">배송 안내</p>
                  <p>5만원 이상 구매 시 무료 배송, 미만 시 3,000원</p>
                  <p>결제 완료 후 1~3 영업일 내 출고</p>
                </div>
                <div>
                  <p className="font-medium text-[#333] mb-1">교환/반품 안내</p>
                  <p>수령 후 7일 이내 교환/반품 가능</p>
                  <p>착용 및 세탁 후 교환/반품 불가</p>
                </div>
              </div>
            )}
            {activeTab === 'review' && (
              <div>
                {/* 평균 평점 */}
                {reviews.length > 0 && (() => {
                  const avg = reviews.reduce((s, r) => s + r.rating, 0) / reviews.length;
                  return (
                    <div className="flex items-center gap-3 mb-6 pb-4 border-b border-[#f0f0f0]">
                      <span className="text-3xl font-bold text-[#222]">{avg.toFixed(1)}</span>
                      <div>
                        <div className="text-yellow-400 text-lg tracking-wide">
                          {'★'.repeat(Math.round(avg))}{'☆'.repeat(5 - Math.round(avg))}
                        </div>
                        <p className="text-xs text-[#999] mt-0.5">리뷰 {reviews.length}개</p>
                      </div>
                    </div>
                  );
                })()}

                {reviews.length === 0 ? (
                  <div className="text-center text-[#bbb] py-8 text-sm">아직 리뷰가 없습니다.</div>
                ) : (
                  <div className="divide-y divide-[#f0f0f0]">
                    {reviews.map((r) => (
                      <div key={r.reviewId} className="py-4">
                        <div className="flex items-center gap-3 mb-1">
                          <span className="text-yellow-400 text-sm tracking-wide">
                            {'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}
                          </span>
                          <span className="text-xs font-medium text-[#333]">{r.userName}</span>
                          <span className="text-xs text-[#bbb] ml-auto">{formatDateTime(r.createdAt)}</span>
                        </div>
                        {r.content && (
                          <p className="text-sm text-[#555] mt-1 whitespace-pre-wrap">{r.content}</p>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {activeTab === 'inquiry' && (
              <div>
                {/* 문의 작성 폼 */}
                <div className="border border-[#e5e5e5] p-5 mb-6">
                  <h3 className="text-sm font-bold text-[#222] mb-4">문의 작성</h3>
                  <div className="space-y-3">
                    <input
                      type="text"
                      value={inquirySubject}
                      onChange={(e) => setInquirySubject(e.target.value)}
                      placeholder="문의 제목을 입력하세요"
                      className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#222]"
                    />
                    <textarea
                      value={inquiryContent}
                      onChange={(e) => setInquiryContent(e.target.value)}
                      placeholder="문의 내용을 입력하세요"
                      rows={4}
                      className="w-full border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#222] resize-none"
                    />
                    <Button variant="primary" size="sm" disabled={submittingInquiry} onClick={handleInquirySubmit}>
                      {submittingInquiry ? '등록 중...' : '문의 등록'}
                    </Button>
                  </div>
                </div>

                {/* 문의 목록 */}
                {inquiries.length === 0 ? (
                  <div className="text-center text-[#bbb] py-8 text-sm">등록된 문의가 없습니다.</div>
                ) : (
                  <div className="divide-y divide-[#f0f0f0]">
                    {inquiries.map((inq) => (
                      <div key={inq.inquiryId} className="py-4">
                        <div className="flex items-center gap-3 mb-1">
                          <span className={`text-xs px-2 py-0.5 ${INQUIRY_STATUS_COLOR[inq.status] ?? ''}`}>
                            {INQUIRY_STATUS_LABEL[inq.status] ?? inq.status}
                          </span>
                          <span className="text-xs font-medium text-[#333]">{inq.subject}</span>
                          <span className="text-xs text-[#bbb] ml-auto">{inq.userName} · {formatDateTime(inq.createdAt)}</span>
                        </div>
                        <p className="text-xs text-[#777] mt-1 whitespace-pre-wrap">{inq.content}</p>
                        {inq.answer && (
                          <div className="bg-[#f7f8fc] border-l-2 border-[#4c74e5] px-4 py-2 mt-2">
                            <span className="text-xs font-bold text-[#4c74e5]">관리자 답변</span>
                            <p className="text-xs text-[#555] mt-0.5 whitespace-pre-wrap">{inq.answer}</p>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </main>

      <ShopFooter />
    </>
  );
}
