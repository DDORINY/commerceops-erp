'use client';

import { useState } from 'react';
import Link from 'next/link';

const BANNERS = [
  {
    id: 1,
    headline: 'NEW SEASON',
    subline: '2026 S/S 신상품 출시',
    description: '봄/여름 시즌 신규 컬렉션을 만나보세요',
    ctaText: '신상품 보러가기',
    ctaHref: '/products?category=NEW',
    bgColor: 'bg-[#f9f0f2]',
    textColor: 'text-[#3a1a22]',
    accentColor: 'bg-[#c4788a]',
  },
  {
    id: 2,
    headline: 'BEST PICKS',
    subline: '이번 시즌 베스트 아이템',
    description: '고객님이 가장 많이 선택한 상품을 소개합니다',
    ctaText: '베스트 상품 보기',
    ctaHref: '/products?category=BEST',
    bgColor: 'bg-[#f0f4f9]',
    textColor: 'text-[#1a2a3a]',
    accentColor: 'bg-[#7899c4]',
  },
  {
    id: 3,
    headline: 'SUMMER SALE',
    subline: '최대 30% 할인',
    description: '한정 수량 세일 상품, 지금 바로 확인하세요',
    ctaText: '세일 상품 보기',
    ctaHref: '/products?category=SALE',
    bgColor: 'bg-[#f5f0eb]',
    textColor: 'text-[#3a2a1a]',
    accentColor: 'bg-[#c4a278]',
  },
];

export default function MainBanner() {
  const [active, setActive] = useState(0);
  const banner = BANNERS[active];

  return (
    <section className={`w-full ${banner.bgColor} transition-colors duration-500`}>
      <div className="max-w-[1200px] mx-auto px-4">
        <div className="flex items-center justify-between py-20 md:py-28 gap-8">
          {/* 텍스트 */}
          <div className="flex-1">
            <p className="text-xs font-semibold tracking-[0.3em] text-[#999] mb-3 uppercase">
              CommerceOps Collection
            </p>
            <h2 className={`text-5xl md:text-6xl font-bold tracking-tight mb-4 ${banner.textColor}`}>
              {banner.headline}
            </h2>
            <p className={`text-xl font-medium mb-3 ${banner.textColor}`}>{banner.subline}</p>
            <p className="text-sm text-[#777] mb-8">{banner.description}</p>
            <Link
              href={banner.ctaHref}
              className={`inline-flex items-center gap-2 ${banner.accentColor} text-white text-sm font-medium px-8 py-3 hover:opacity-90 transition-opacity`}
            >
              {banner.ctaText}
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>
          </div>

          {/* 배너 이미지 플레이스홀더 */}
          <div className="hidden md:block w-[360px] h-[440px] bg-white/40 flex-shrink-0 flex items-center justify-center">
            <div className="w-full h-full flex items-center justify-center text-[#bbb] text-sm">
              Banner Image
            </div>
          </div>
        </div>

        {/* 인디케이터 */}
        <div className="flex items-center justify-center gap-2 pb-8">
          {BANNERS.map((_, i) => (
            <button
              key={i}
              onClick={() => setActive(i)}
              className={[
                'h-[3px] transition-all duration-300',
                i === active ? 'w-8 bg-[#222]' : 'w-4 bg-[#ccc]',
              ].join(' ')}
            />
          ))}
        </div>
      </div>
    </section>
  );
}
