'use client';

import { ReactNode, useEffect, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import AdminSidebarV2 from './AdminSidebarV2';
import AdminTopbar from './AdminTopbar';
import { getAccessToken, getUserRole } from '@/lib/auth';

interface AdminLayoutProps {
  children: ReactNode;
  title: string;
}

export default function AdminLayout({ children, title }: AdminLayoutProps) {
  const router = useRouter();
  const pathname = usePathname();
  const [status, setStatus] = useState<'checking' | 'allowed' | 'forbidden'>('checking');

  useEffect(() => {
    const token = getAccessToken();
    if (!token) {
      sessionStorage.setItem('authMessage', '관리자 화면은 로그인이 필요합니다.');
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
      return;
    }

    const role = getUserRole();
    if (role && role !== 'ADMIN' && role !== 'SUPER_ADMIN' && role !== 'MANAGER') {
      queueMicrotask(() => setStatus('forbidden'));
      return;
    }

    queueMicrotask(() => setStatus('allowed'));
  }, [pathname, router]);

  if (status === 'checking') {
    return (
      <div className="min-h-screen bg-[#f4f5f9] flex items-center justify-center">
        <p className="text-sm text-[#6f7a8a]">인증 상태를 확인하고 있습니다.</p>
      </div>
    );
  }

  if (status === 'forbidden') {
    return (
      <div className="min-h-screen bg-[#f4f5f9] flex items-center justify-center px-4">
        <div className="max-w-[420px] w-full border border-[#e1e4ea] bg-white p-6 text-center">
          <h1 className="text-base font-semibold text-[#1a1f2e]">접근 권한이 없습니다</h1>
          <p className="mt-2 text-sm text-[#6f7a8a]">관리자 권한이 있는 계정으로 다시 로그인해주세요.</p>
          <button
            type="button"
            onClick={() => router.replace('/login')}
            className="mt-5 h-10 px-4 bg-[#1a1f2e] text-white text-sm font-medium hover:bg-[#2b3347] transition-colors"
          >
            로그인으로 이동
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-[#f4f5f9]">
      <AdminSidebarV2 />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <AdminTopbar title={title} />
        <main className="flex-1 overflow-y-auto p-6">{children}</main>
      </div>
    </div>
  );
}
