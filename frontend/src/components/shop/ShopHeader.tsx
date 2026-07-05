'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import DynamicCategoryNav from './DynamicCategoryNav';
import { clearAuth, getStoredUser } from '@/lib/auth';
import { authService } from '@/lib/services/authService';
import type { User } from '@/features/auth/types';

const canEnterAdmin = (user: User | null) =>
  user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN' || user?.role === 'MANAGER';

export default function ShopHeader() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    queueMicrotask(() => setUser(getStoredUser()));
  }, []);

  const handleLogout = async () => {
    try {
      await authService.logout();
    } catch {
      // 서버 로그아웃 API 실패 여부와 관계없이 클라이언트 세션은 정리한다.
    } finally {
      clearAuth();
      setUser(null);
      sessionStorage.setItem('authMessage', '로그아웃되었습니다.');
      router.push('/login');
    }
  };

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    const keyword = searchQuery.trim();
    router.push(keyword ? `/products?keyword=${encodeURIComponent(keyword)}` : '/products');
  };

  return (
    <header className="w-full border-b border-[#e5e5e5] bg-white sticky top-0 z-50">
      <div className="border-b border-[#f0f0f0]">
        <div className="max-w-[1200px] mx-auto px-4 h-9 flex items-center justify-end gap-4">
          {user ? (
            <>
              <span className="text-xs text-[#777]">{user.name}님</span>
              <span className="text-[#e0e0e0] text-xs">|</span>
              <button type="button" onClick={handleLogout} className="text-xs text-[#777] hover:text-[#222] transition-colors">
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="text-xs text-[#777] hover:text-[#222] transition-colors">로그인</Link>
              <span className="text-[#e0e0e0] text-xs">|</span>
              <Link href="/signup" className="text-xs text-[#777] hover:text-[#222] transition-colors">회원가입</Link>
            </>
          )}
          <span className="text-[#e0e0e0] text-xs">|</span>
          <Link href="/orders/guest" className="text-xs text-[#777] hover:text-[#222] transition-colors">주문조회</Link>
          <span className="text-[#e0e0e0] text-xs">|</span>
          <Link href="/mypage" className="text-xs text-[#777] hover:text-[#222] transition-colors">마이페이지</Link>
          {canEnterAdmin(user) && (
            <>
              <span className="text-[#e0e0e0] text-xs">|</span>
              <Link href="/admin" className="text-xs text-[#f3a6b8] hover:text-[#d97b93] font-medium transition-colors">
                관리자
              </Link>
            </>
          )}
        </div>
      </div>

      <div className="max-w-[1200px] mx-auto px-4 h-[72px] flex items-center justify-between gap-6">
        <Link href="/" className="flex-shrink-0">
          <span className="text-[22px] font-bold tracking-widest text-[#222] uppercase">CommerceOps</span>
        </Link>

        <form onSubmit={handleSearch} className="flex-1 max-w-[420px]">
          <div className="flex border border-[#222] h-10">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="상품명을 검색하세요"
              className="flex-1 px-4 text-sm outline-none bg-white text-[#222] placeholder:text-[#bbb]"
            />
            <button type="submit" className="w-11 bg-[#222] flex items-center justify-center text-white hover:bg-[#444] transition-colors" aria-label="상품 검색">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </button>
          </div>
        </form>

        <div className="flex items-center gap-5">
          <Link href="/mypage" className="flex flex-col items-center gap-0.5 text-[#555] hover:text-[#222] transition-colors">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            <span className="text-[10px] tracking-wide">마이페이지</span>
          </Link>
          <Link href="/cart" className="flex flex-col items-center gap-0.5 text-[#555] hover:text-[#222] transition-colors relative">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
            <span className="text-[10px] tracking-wide">장바구니</span>
          </Link>
        </div>
      </div>

      <DynamicCategoryNav />
    </header>
  );
}
