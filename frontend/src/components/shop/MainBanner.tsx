'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { bannerService, type ApiMainBanner } from '@/lib/services/bannerService';

const FALLBACK_BANNERS: ApiMainBanner[] = [
  {
    id: 0,
    title: '새 시즌 컬렉션',
    subtitle: 'CommerceOps 추천 기획전',
    description: '관리자 배너가 없을 때 표시되는 기본 배너입니다.',
    imageUrl: null,
    linkUrl: '/products',
    position: 'MAIN_TOP',
    sortOrder: 0,
    active: true,
    startsAt: null,
    endsAt: null,
    createdAt: '',
    updatedAt: '',
  },
];

export default function MainBanner() {
  const [banners, setBanners] = useState<ApiMainBanner[]>(FALLBACK_BANNERS);
  const [active, setActive] = useState(0);

  useEffect(() => {
    let mounted = true;
    bannerService
      .getBanners()
      .then((items) => {
        if (!mounted) return;
        setBanners(items.length > 0 ? items : FALLBACK_BANNERS);
        setActive(0);
      })
      .catch(() => {
        if (!mounted) return;
        setBanners(FALLBACK_BANNERS);
        setActive(0);
      });

    return () => {
      mounted = false;
    };
  }, []);

  const banner = banners[Math.min(active, banners.length - 1)] ?? FALLBACK_BANNERS[0];
  const content = (
    <div className="max-w-[1200px] mx-auto px-4">
      <div className="grid grid-cols-1 md:grid-cols-[1fr_360px] items-center py-16 md:py-24 gap-8">
        <div>
          <p className="text-xs font-semibold tracking-[0.3em] text-[#999] mb-3 uppercase">
            CommerceOps 배너
          </p>
          <h2 className="text-4xl md:text-5xl font-bold tracking-tight mb-4 text-[#222]">
            {banner.title}
          </h2>
          {banner.subtitle && (
            <p className="text-lg font-medium mb-3 text-[#444]">{banner.subtitle}</p>
          )}
          {banner.description && (
            <p className="text-sm text-[#777] mb-8 leading-6">{banner.description}</p>
          )}
          {banner.linkUrl && (
            <span className="inline-flex items-center gap-2 bg-[#222] text-white text-sm font-medium px-8 py-3 hover:bg-[#444] transition-colors">
              자세히 보기
            </span>
          )}
        </div>

        <div className="hidden md:flex w-full aspect-[4/5] bg-[#f7f8fc] border border-[#e8eaf0] items-center justify-center overflow-hidden">
          {banner.imageUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={banner.imageUrl} alt={banner.title} className="h-full w-full object-cover" />
          ) : (
            <span className="text-sm text-[#8a9bb5]">배너 이미지</span>
          )}
        </div>
      </div>

      {banners.length > 1 && (
        <div className="flex items-center justify-center gap-2 pb-8">
          {banners.map((item, i) => (
            <button
              key={item.id}
              type="button"
              aria-label={`${i + 1}번 배너 보기`}
              onClick={() => setActive(i)}
              className={[
                'h-[3px] transition-all duration-300',
                i === active ? 'w-8 bg-[#222]' : 'w-4 bg-[#ccc]',
              ].join(' ')}
            />
          ))}
        </div>
      )}
    </div>
  );

  return (
    <section className="w-full bg-[#f9f0f2] transition-colors duration-500">
      {banner.linkUrl ? (
        <Link href={banner.linkUrl} className="block">
          {content}
        </Link>
      ) : (
        content
      )}
    </section>
  );
}
