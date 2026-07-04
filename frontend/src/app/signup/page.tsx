'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import { authService } from '@/lib/services/authService';

export default function SignupPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    name: '',
    phone: '',
  });
  const [agreeAll, setAgreeAll] = useState(false);
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (form.password !== form.confirmPassword) {
      setApiError('비밀번호가 일치하지 않습니다.');
      return;
    }
    if (!agreeAll) {
      setApiError('필수 약관에 동의해주세요.');
      return;
    }

    try {
      setLoading(true);
      setApiError('');
      await authService.signup({
        email: form.email,
        password: form.password,
        name: form.name,
        phone: form.phone || undefined,
      });
      router.push('/login');
    } catch (err) {
      setApiError(err instanceof Error ? err.message : '회원가입에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: string, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  return (
    <>
      <ShopHeader />

      <main className="max-w-[1200px] mx-auto px-4 py-16">
        <div className="max-w-[440px] mx-auto">
          <div className="text-center mb-10">
            <h1 className="text-2xl font-bold text-[#222] tracking-widest mb-2">JOIN</h1>
            <p className="text-sm text-[#999]">CommerceOps 회원가입</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="이름"
              value={form.name}
              onChange={(e) => handleChange('name', e.target.value)}
              placeholder="이름 입력"
              fullWidth
            />
            <Input
              label="이메일"
              type="email"
              value={form.email}
              onChange={(e) => handleChange('email', e.target.value)}
              placeholder="이메일 주소 입력"
              fullWidth
            />
            <Input
              label="비밀번호"
              type="password"
              value={form.password}
              onChange={(e) => handleChange('password', e.target.value)}
              placeholder="8자 이상 입력"
              fullWidth
            />
            <Input
              label="비밀번호 확인"
              type="password"
              value={form.confirmPassword}
              onChange={(e) => handleChange('confirmPassword', e.target.value)}
              placeholder="비밀번호 재입력"
              fullWidth
            />
            <Input
              label="연락처"
              type="tel"
              value={form.phone}
              onChange={(e) => handleChange('phone', e.target.value)}
              placeholder="010-0000-0000"
              fullWidth
            />

            <div className="border border-[#e5e5e5] p-4 space-y-3 mt-6">
              <label className="flex items-center gap-2.5 cursor-pointer">
                <input
                  type="checkbox"
                  checked={agreeAll}
                  onChange={(e) => setAgreeAll(e.target.checked)}
                  className="w-4 h-4 accent-[#222]"
                />
                <span className="text-sm font-medium text-[#222]">전체 동의</span>
              </label>
              <div className="border-t border-[#f0f0f0] pt-3 space-y-2">
                {[
                  { label: '[필수] 이용약관 동의', required: true },
                  { label: '[필수] 개인정보 수집 및 이용 동의', required: true },
                  { label: '[선택] 마케팅 정보 수신 동의', required: false },
                ].map((term) => (
                  <label key={term.label} className="flex items-center gap-2.5 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={agreeAll}
                      readOnly
                      className="w-3.5 h-3.5 accent-[#222]"
                    />
                    <span className="text-xs text-[#777]">{term.label}</span>
                  </label>
                ))}
              </div>
            </div>

            {apiError && (
              <p className="text-sm text-[#d94f4f] text-center">{apiError}</p>
            )}

            <div className="pt-2">
              <Button type="submit" variant="primary" size="lg" fullWidth disabled={loading}>
                {loading ? '처리 중...' : '회원가입'}
              </Button>
            </div>
          </form>

          <p className="text-center text-sm text-[#999] mt-6">
            이미 회원이신가요?{' '}
            <Link href="/login" className="text-[#222] font-medium hover:underline">
              로그인
            </Link>
          </p>
        </div>
      </main>

      <ShopFooter />
    </>
  );
}
