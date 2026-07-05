'use client';

import { use, useState, useEffect } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import Button from '@/components/common/Button';
import { productService, type ApiProductDetail, type ProductDetailBlock } from '@/lib/services/productService';
import { cartService } from '@/lib/services/cartService';
import { inquiryService, type ApiInquiry } from '@/lib/services/inquiryService';
import { reviewService, type ApiReview } from '@/lib/services/reviewService';
import { wishlistService } from '@/lib/services/wishlistService';
import { formatPrice, formatDateTime, INQUIRY_STATUS_LABEL, INQUIRY_STATUS_COLOR } from '@/lib/format';

function parseSpecRows(specJson?: string | null): { label: string; value: string }[] {
  if (!specJson) return [];
  try {
    const parsed = JSON.parse(specJson);
    if (!Array.isArray(parsed)) return [];
    return parsed
      .map((row) => ({
        label: String(row?.label ?? ''),
        value: String(row?.value ?? ''),
      }))
      .filter((row) => row.label || row.value);
  } catch {
    return [];
  }
}

function ProductDetailBlockView({ block }: { block: ProductDetailBlock }) {
  if (block.blockType === 'HEADING') {
    return (
      <section>
        <h2 className="text-xl font-bold text-[#222] mb-3">{block.title || block.content}</h2>
      </section>
    );
  }

  if (block.blockType === 'TEXT') {
    return (
      <section>
        {block.title && <h3 className="text-base font-bold text-[#222] mb-2">{block.title}</h3>}
        <p className="whitespace-pre-wrap text-[#555]">{block.content}</p>
      </section>
    );
  }

  if (block.blockType === 'IMAGE') {
    const src = block.imageUrl || 'https://placehold.co/900x560?text=No+Image';
    return (
      <section>
        {block.title && <h3 className="text-base font-bold text-[#222] mb-3">{block.title}</h3>}
        <div className="relative aspect-[16/10] bg-[#f7f7f7]">
          <Image src={src} alt={block.title || '?곹뭹 ?곸꽭 ?대?吏'} fill className="object-cover" sizes="100vw" />
        </div>
      </section>
    );
  }

  if (block.blockType === 'NOTICE') {
    return (
      <section className="border border-[#e8eaf0] bg-[#f7f8fc] px-4 py-3">
        {block.title && <p className="font-bold text-[#333] mb-1">{block.title}</p>}
        <p className="whitespace-pre-wrap text-[#555]">{block.content}</p>
      </section>
    );
  }

  if (block.blockType === 'SPEC_TABLE') {
    const rows = parseSpecRows(block.specJson);
    if (rows.length === 0) return null;
    return (
      <section>
        {block.title && <h3 className="text-base font-bold text-[#222] mb-3">{block.title}</h3>}
        <div className="border border-[#e5e5e5] divide-y divide-[#e5e5e5]">
          {rows.map((row, index) => (
            <div key={`${row.label}-${index}`} className="grid grid-cols-[140px_1fr] text-sm">
              <div className="bg-[#fafafa] px-4 py-3 text-[#777]">{row.label}</div>
              <div className="px-4 py-3 text-[#444]">{row.value}</div>
            </div>
          ))}
        </div>
      </section>
    );
  }

  if (block.blockType === 'HTML') {
    return (
      <section>
        {block.title && <h3 className="text-base font-bold text-[#222] mb-2">{block.title}</h3>}
        {/* Admin-authored internal CMS HTML. Keep this endpoint admin-only and document the XSS risk. */}
        <div className="prose max-w-none" dangerouslySetInnerHTML={{ __html: block.content || '' }} />
      </section>
    );
  }

  return null;
}

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
  const [actionMessage, setActionMessage] = useState('');
  const [tabError, setTabError] = useState('');

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
          const updated = [{
            id: p.id,
            name: p.name,
            price: p.price,
            imageUrl: p.imageUrl ?? 'https://placehold.co/600x750?text=No+Image',
            categoryName: p.categoryName,
          }, ...filtered].slice(0, 10);
          localStorage.setItem('recent_products', JSON.stringify(updated));
        } catch { /* ignore */ }
      })
      .catch(() => {
        setNotFound(true);
        setLoading(false);
      });

    // Check wishlist status
    const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
    if (token) {
      wishlistService.getStatus(Number(id)).then((res) => setLiked(res.liked)).catch(() => {});
    }
  }, [id]);

  useEffect(() => {
    if (activeTab !== 'inquiry' && activeTab !== 'review') {
      return;
    }
    if (activeTab === 'inquiry') {
      inquiryService.getProductInquiries(Number(id)).then(setInquiries).catch(() => {
        setInquiries([]);
        setTabError('臾몄쓽 紐⑸줉??遺덈윭?ㅼ? 紐삵뻽?듬땲?? ?좎떆 ???ㅼ떆 ?쒕룄?댁＜?몄슂.');
      });
    }
    if (activeTab === 'review') {
      reviewService.getProductReviews(Number(id), 0, 20).then((res) => {
        setReviews(res.content);
        setReviewTotalElements(res.totalElements);
      }).catch(() => {
        setReviews([]);
        setReviewTotalElements(0);
        setTabError('由щ럭 紐⑸줉??遺덈윭?ㅼ? 紐삵뻽?듬땲?? ?좎떆 ???ㅼ떆 ?쒕룄?댁＜?몄슂.');
      });
    }
  }, [activeTab, id]);

  const handleInquirySubmit = async () => {
    if (!inquirySubject.trim() || !inquiryContent.trim()) {
      setActionMessage('?쒕ぉ怨??댁슜??紐⑤몢 ?낅젰?섏꽭??');
      return;
    }
    setSubmittingInquiry(true);
    try {
      await inquiryService.createProductInquiry(Number(id), 'PRODUCT', inquirySubject.trim(), inquiryContent.trim());
      setInquirySubject('');
      setInquiryContent('');
      inquiryService.getProductInquiries(Number(id)).then(setInquiries).catch(() => {});
      setActionMessage('臾몄쓽媛 ?깅줉?섏뿀?듬땲??');
    } catch (err) {
      setActionMessage(err instanceof Error ? err.message : '臾몄쓽 ?깅줉???ㅽ뙣?덉뒿?덈떎.');
    } finally {
      setSubmittingInquiry(false);
    }
  };

  const handleToggleWishlist = async () => {
    setWishlistLoading(true);
    try {
      const res = await wishlistService.toggle(Number(id));
      setLiked(res.liked);
      setActionMessage(res.liked ? '李?紐⑸줉??異붽??덉뒿?덈떎.' : '李?紐⑸줉?먯꽌 ?쒓굅?덉뒿?덈떎.');
    } catch {
      setActionMessage('濡쒓렇?몄씠 ?꾩슂???쒕퉬?ㅼ엯?덈떎.');
    } finally {
      setWishlistLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!product) return;
    if (!isPurchasable) {
      setActionMessage(stockLabel);
      return;
    }
    // ?듭뀡???덈뒗 ?곹뭹?몃뜲 ?좏깮?섏? ?딆? ?듭뀡???덉쑝硫?寃쎄퀬
    if (product.options?.length > 0) {
      const missing = product.options.find((og) => !selectedOptions[og.name]);
      if (missing) {
        setActionMessage(`"${missing.name}" ?듭뀡???좏깮?댁＜?몄슂.`);
        return;
      }
    }
    try {
      setAddingToCart(true);
      const opts = Object.keys(selectedOptions).length > 0 ? selectedOptions : undefined;
      await cartService.addToCart(product.id, quantity, opts);
      setActionMessage(`"${product.name}" ?λ컮援щ땲??異붽??먯뒿?덈떎.`);
    } catch (err) {
      setActionMessage(err instanceof Error ? err.message : '?λ컮援щ땲 異붽????ㅽ뙣?덉뒿?덈떎.');
    } finally {
      setAddingToCart(false);
    }
  };

  const handleTabChange = (tab: 'detail' | 'shipping' | 'review' | 'inquiry') => {
    setTabError('');
    setActiveTab(tab);
  };

  if (loading) {
    return (
      <>
        <ShopHeader />
        <div className="max-w-[1200px] mx-auto px-4 py-20 text-center text-[#aaa]">
          ?곹뭹??遺덈윭?ㅻ뒗 以?..
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
          ?곹뭹??李얠쓣 ???놁뒿?덈떎.
          <br />
          <Link href="/products" className="text-[#222] underline mt-4 inline-block">
            紐⑸줉?쇰줈 ?뚯븘媛湲?
          </Link>
        </div>
        <ShopFooter />
      </>
    );
  }

  const isPurchasable = product.purchasable ?? (product.status !== 'SOLD_OUT' && product.stockQuantity > 0);
  const isSoldOut = product.stockDisplayStatus === 'SOLD_OUT' || product.status === 'SOLD_OUT' || product.stockQuantity === 0;
  const stockLabel = product.stockDisplayText ?? (isSoldOut ? '품절' : '구매 가능');
  const imageSrc = product.imageUrl || 'https://placehold.co/600x750?text=No+Image';

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
              src={imageSrc}
              alt={product.name}
              fill
              className="object-cover"
              sizes="(max-width: 768px) 100vw, 50vw"
              priority
            />
            {isSoldOut && (
              <div className="absolute inset-0 bg-white/60 flex items-center justify-center">
                <span className="bg-[#777] text-white text-sm font-medium px-6 py-2 tracking-widest">
                  ?덉젅
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

            <p className="text-sm text-[#999] mb-6">諛곗넚鍮?3,000??(5留뚯썝 ?댁긽 臾대즺)</p>

            <div className="border-t border-[#f0f0f0] pt-6 space-y-4">
              {/* ?듭뀡 ?좏깮 */}
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
                <span className="w-20 text-[#999]">?ш퀬 ?꾪솴</span>
                <span className={isSoldOut ? 'text-[#d94f4f] font-medium' : 'text-[#222]'}>
                  {isSoldOut ? '?덉젅' : `${product.stockQuantity}媛??⑥쓬`}
                </span>
              </div>

              {isPurchasable && (
                <div className="flex items-center gap-4 text-sm">
                  <span className="w-20 text-[#999]">?섎웾</span>
                  <div className="flex items-center border border-[#ddd]">
                    <button
                      onClick={() => setQuantity(Math.max(1, quantity - 1))}
                      className="w-10 h-10 flex items-center justify-center text-[#555] hover:bg-[#f5f5f5] transition-colors"
                    >
                      ??
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

            {isPurchasable && (
              <div className="border-t border-[#f0f0f0] mt-6 pt-4 flex items-center justify-between">
                <span className="text-sm text-[#999]">?⑷퀎</span>
                <span className="text-xl font-bold text-[#222]">
                  {formatPrice(product.price * quantity)}
                </span>
              </div>
            )}

            <div className="mt-6 flex flex-col gap-3">
              {actionMessage && (
                <p className="border border-[#f0d6d6] bg-[#fff7f7] px-3 py-2 text-xs text-[#c43a3a]">
                  {actionMessage}
                </p>
              )}
              <div className="flex gap-3">
                <Button
                  variant="secondary"
                  size="lg"
                  fullWidth
                  disabled={!isPurchasable || addingToCart}
                  onClick={handleAddToCart}
                >
                  {!isPurchasable ? stockLabel : addingToCart ? '추가 중...' : '장바구니 담기'}
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
              {isPurchasable && (
                <Button variant="primary" size="lg" fullWidth>
                  바로 구매하기
                </Button>
              )}
            </div>
          </div>
        </div>

        <div>
          <div className="flex border-b border-[#e5e5e5] mb-8">
            {([['detail', '?곹뭹 ?곸꽭'], ['shipping', '諛곗넚/援먰솚/諛섑뭹'], ['review', `由щ럭 ${reviewTotalElements > 0 ? `(${reviewTotalElements})` : ''}`], ['inquiry', '?곹뭹 臾몄쓽']] as const).map(
              ([tab, label]) => (
                <button
                  key={tab}
                  onClick={() => handleTabChange(tab)}
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
            {tabError && (
              <div className="mb-5 border border-[#f0d6d6] bg-[#fff7f7] px-4 py-3 text-sm text-[#c43a3a]">
                {tabError}
              </div>
            )}
            {activeTab === 'detail' && (
              <div className="max-w-[760px] space-y-8">
                {product.detailBlocks?.length > 0 ? (
                  product.detailBlocks.map((block, index) => (
                    <ProductDetailBlockView key={block.id ?? `${block.blockType}-${index}`} block={block} />
                  ))
                ) : (
                  <div className="max-w-[600px]">
                    <p className="mb-4">{product.description || `${product.name} 상품 상세 설명입니다.`}</p>
                    <ul className="space-y-2 text-[#777]">
                      <li>소재: 상품별 상세 정보를 확인하세요.</li>
                      <li>관리 방법: 상품 안내 기준을 따르세요.</li>
                      <li>원산지: 상품 정보 참조</li>
                      <li>제조사: 상품 정보 참조</li>
                    </ul>
                  </div>
                )}
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
                  <p>착용 및 훼손 시 교환/반품이 제한될 수 있습니다.</p>
                </div>
              </div>
            )}
            {activeTab === 'review' && (
              <div>
                {/* ?됯퇏 ?됱젏 */}
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
                  <div className="text-center text-[#bbb] py-8 text-sm">?꾩쭅 由щ럭媛 ?놁뒿?덈떎.</div>
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
                {/* 臾몄쓽 ?묒꽦 ??*/}
                <div className="border border-[#e5e5e5] p-5 mb-6">
                  <h3 className="text-sm font-bold text-[#222] mb-4">臾몄쓽 ?묒꽦</h3>
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
                      {submittingInquiry ? '?깅줉 以?..' : '臾몄쓽 ?깅줉'}
                    </Button>
                  </div>
                </div>

                {/* 臾몄쓽 紐⑸줉 */}
                {inquiries.length === 0 ? (
                  <div className="text-center text-[#bbb] py-8 text-sm">?깅줉??臾몄쓽媛 ?놁뒿?덈떎.</div>
                ) : (
                  <div className="divide-y divide-[#f0f0f0]">
                    {inquiries.map((inq) => (
                      <div key={inq.inquiryId} className="py-4">
                        <div className="flex items-center gap-3 mb-1">
                          <span className={`text-xs px-2 py-0.5 ${INQUIRY_STATUS_COLOR[inq.status] ?? ''}`}>
                            {INQUIRY_STATUS_LABEL[inq.status] ?? inq.status}
                          </span>
                          <span className="text-xs font-medium text-[#333]">{inq.subject}</span>
                          <span className="text-xs text-[#bbb] ml-auto">{inq.userName} 쨌 {formatDateTime(inq.createdAt)}</span>
                        </div>
                        <p className="text-xs text-[#777] mt-1 whitespace-pre-wrap">{inq.content}</p>
                        {inq.answer && (
                          <div className="bg-[#f7f8fc] border-l-2 border-[#4c74e5] px-4 py-2 mt-2">
                            <span className="text-xs font-bold text-[#4c74e5]">愿由ъ옄 ?듬?</span>
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






