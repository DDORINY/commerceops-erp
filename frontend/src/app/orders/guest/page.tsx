import Link from 'next/link';
import ShopHeader from '@/components/shop/ShopHeader';
import ShopFooter from '@/components/shop/ShopFooter';
import Button from '@/components/common/Button';

export default function GuestOrdersPage() {
  return (
    <>
      <ShopHeader />
      <main className="max-w-[720px] mx-auto px-4 py-16">
        <div className="border border-[#e5e5e5] p-8">
          <h1 className="text-xl font-bold text-[#222] mb-3">비회원 주문조회</h1>
          <p className="text-sm text-[#666] leading-6 mb-6">
            비회원 주문은 주문번호와 주문 시 입력한 연락처 또는 비밀번호로 조회할 수 있도록 준비 중입니다.
            현재는 회원 주문 조회만 제공되며, 비회원 주문 생성/조회 API는 후속 주문 버전에서 구현합니다.
          </p>

          <div className="space-y-3 text-sm text-[#777] bg-[#fafafa] border border-[#eee] p-4 mb-6">
            <p>예정 조회 정보: 주문번호, 주문자 연락처, 비회원 주문 비밀번호</p>
            <p>예정 흐름: 비회원 주문 생성 후 주문번호 발급, 주문번호 기반 조회</p>
          </div>

          <div className="flex flex-col sm:flex-row gap-3">
            <Link href="/products" className="sm:flex-1">
              <Button variant="outline" fullWidth>쇼핑 계속하기</Button>
            </Link>
            <Link href="/login?next=/orders" className="sm:flex-1">
              <Button variant="primary" fullWidth>회원 주문조회</Button>
            </Link>
          </div>
        </div>
      </main>
      <ShopFooter />
    </>
  );
}
