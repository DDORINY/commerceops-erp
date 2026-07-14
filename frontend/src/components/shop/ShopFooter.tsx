import Link from 'next/link';

export default function ShopFooter() {
  return (
    <footer className="border-t border-[#e5e5e5] bg-white mt-12 sm:mt-20">
      <div className="max-w-[1200px] mx-auto px-4 py-8 sm:py-12">
        {/* 링크 섹션 */}
        <div className="flex flex-wrap gap-x-5 gap-y-3 sm:gap-8 mb-8 justify-center text-xs sm:text-sm text-[#555]">
          <Link href="#" className="hover:text-[#222] transition-colors">이용약관</Link>
          <Link href="#" className="font-medium hover:text-[#222] transition-colors">개인정보처리방침</Link>
          <Link href="#" className="hover:text-[#222] transition-colors">입점/제휴 문의</Link>
          <Link href="#" className="hover:text-[#222] transition-colors">고객센터</Link>
          <Link href="#" className="hover:text-[#222] transition-colors">공지사항</Link>
        </div>

        {/* 회사 정보 */}
        <div className="text-center text-xs text-[#aaa] leading-relaxed">
          <p className="mb-1">
            상호: (주)커머스옵스 &nbsp;|&nbsp; 대표: 홍길동 &nbsp;|&nbsp; 사업자등록번호: 123-45-67890
          </p>
          <p className="mb-1">
            통신판매업신고번호: 2026-서울강남-0001 &nbsp;|&nbsp; 주소: 서울특별시 강남구 테헤란로 123
          </p>
          <p className="mb-1">
            고객센터: 02-1234-5678 &nbsp;|&nbsp; 운영시간: 평일 10:00 ~ 18:00 (점심 12:00 ~ 13:00)
          </p>
          <p className="mt-4 text-[#ccc]">&copy; 2026 CommerceOps. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
