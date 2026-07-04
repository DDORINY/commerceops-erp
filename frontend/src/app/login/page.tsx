'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import { authService } from '@/lib/services/authService';
import { setAccessToken, setRefreshToken, setStoredUser } from '@/lib/auth';
import type { User } from '@/features/auth/types';

export default function LoginPage() {
  const router = useRouter();
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({ email: '', password: '' });
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const authMessage = sessionStorage.getItem('authMessage') ?? '';
    sessionStorage.removeItem('authMessage');
    if (authMessage) {
      queueMicrotask(() => setApiError(authMessage));
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors = { email: '', password: '' };
    if (!form.email) newErrors.email = '이메일을 입력해주세요.';
    if (!form.password) newErrors.password = '비밀번호를 입력해주세요.';

    if (newErrors.email || newErrors.password) {
      setErrors(newErrors);
      return;
    }

    try {
      setLoading(true);
      setApiError('');
      const res = await authService.login({ email: form.email, password: form.password });
      setAccessToken(res.accessToken);
      setRefreshToken(res.refreshToken);
      const user: User = {
        id: res.user.id,
        email: res.user.email,
        name: res.user.name,
        role: res.user.role as User['role'],
        phone: '',
        createdAt: '',
      };
      setStoredUser(user);
      router.push('/');
    } catch (err) {
      setApiError(err instanceof Error ? err.message : '로그인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <ShopHeader />

      <main className="max-w-[1200px] mx-auto px-4 py-16">
        <div className="max-w-[400px] mx-auto">
          <div className="text-center mb-10">
            <h1 className="text-2xl font-bold text-[#222] tracking-widest mb-2">LOGIN</h1>
            <p className="text-sm text-[#999]">CommerceOps 회원 로그인</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="이메일"
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              placeholder="이메일 주소 입력"
              error={errors.email}
              fullWidth
            />
            <Input
              label="비밀번호"
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="비밀번호 입력"
              error={errors.password}
              fullWidth
            />

            {apiError && (
              <p className="text-sm text-[#d94f4f] text-center">{apiError}</p>
            )}

            <div className="pt-2">
              <Button type="submit" variant="primary" size="lg" fullWidth disabled={loading}>
                {loading ? '로그인 중...' : '로그인'}
              </Button>
            </div>
          </form>

          <div className="flex items-center justify-center gap-4 mt-6 text-sm text-[#999]">
            <Link href="/signup" className="hover:text-[#222] transition-colors">
              회원가입
            </Link>
            <span>|</span>
            <span className="cursor-pointer hover:text-[#222] transition-colors">
              아이디 찾기
            </span>
            <span>|</span>
            <span className="cursor-pointer hover:text-[#222] transition-colors">
              비밀번호 찾기
            </span>
          </div>

          <div className="flex items-center gap-4 mt-8">
            <div className="flex-1 h-px bg-[#e5e5e5]" />
            <span className="text-xs text-[#bbb]">또는</span>
            <div className="flex-1 h-px bg-[#e5e5e5]" />
          </div>

          <div className="mt-4 space-y-3">
            <button className="w-full py-3 border border-[#e5e5e5] text-sm text-[#555] hover:border-[#ccc] hover:bg-[#fafafa] transition-colors flex items-center justify-center gap-2">
              <span className="font-medium text-[#FEE500]">K</span>
              카카오 로그인
            </button>
            <button className="w-full py-3 border border-[#e5e5e5] text-sm text-[#555] hover:border-[#ccc] hover:bg-[#fafafa] transition-colors flex items-center justify-center gap-2">
              <span className="font-medium text-[#03C75A]">N</span>
              네이버 로그인
            </button>
          </div>
        </div>
      </main>

      <ShopFooter />
    </>
  );
}
