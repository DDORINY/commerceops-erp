'use client';

import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';

type SettingCard = {
  title: string;
  description: string;
  href?: string;
  status: string;
  fields?: string[];
};

const SETTING_CARDS: SettingCard[] = [
  {
    title: '사업자 설정',
    description: '쇼핑몰 하단과 운영 문서에 사용할 사업자 정보를 정의합니다.',
    status: '저장 기능은 후속 settings 버전에서 구현',
    fields: ['상호명', '대표자명', '사업자등록번호', '통신판매업 신고번호', '주소', '고객센터 전화번호', '이메일'],
  },
  {
    title: '약관 설정',
    description: '이용약관, 개인정보처리방침, 배송/반품 정책 문서의 관리 진입점입니다.',
    status: '약관 버전 관리와 저장은 후속 구현',
    fields: ['이용약관', '개인정보처리방침', '배송/반품 정책'],
  },
  {
    title: '개인정보처리방침 설정',
    description: '개인정보 수집/보관/파기 안내 문구를 관리할 예정입니다.',
    status: 'v0.4 이후 정책 관리로 이관',
  },
  {
    title: '배송/반품 정책 설정',
    description: '배송비, 출고일, 교환/반품 기준을 운영 정책으로 분리할 예정입니다.',
    status: 'v0.4 이후 설정 저장 구조로 이관',
  },
  {
    title: '관리자 작업 이력',
    description: '리뷰 숨김/해제/삭제 등 관리자 작업 이력을 확인합니다.',
    href: '/admin/settings/audit-logs',
    status: '기존 audit_logs API 연결',
  },
  {
    title: '직원/권한 관리',
    description: '직원, 부서, 직급, 권한 그룹, 역할별 메뉴 권한 관리 진입점입니다.',
    status: '상세 DB와 권한 매트릭스는 v0.4.0으로 이관',
    fields: ['직원 관리', '부서 관리', '직급 관리', '권한 그룹 관리', '역할/권한 설정'],
  },
];

export default function AdminSettingsPage() {
  return (
    <AdminLayout title="시스템 설정">
      <div className="space-y-5">
        <div className="border border-[#e8eaf0] bg-white p-5">
          <h2 className="text-base font-semibold text-[#1a1f2e]">설정 기반 정리</h2>
          <p className="mt-2 text-sm text-[#6f7a8a]">
            v0.3.5.1에서는 관리자 메뉴와 설정 진입점을 먼저 정리합니다. 실제 사업자/약관 저장,
            직원/부서/직급/권한 DB 구현은 v0.4.0 이후 작업으로 분리합니다.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {SETTING_CARDS.map((card) => {
            const content = (
              <div className="h-full border border-[#e8eaf0] bg-white p-5 hover:border-[#cfd6e6] transition-colors">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h3 className="text-sm font-semibold text-[#1a1f2e]">{card.title}</h3>
                    <p className="mt-2 text-sm text-[#6f7a8a] leading-relaxed">{card.description}</p>
                  </div>
                  {card.href && <span className="text-xs text-[#4c74e5] font-medium shrink-0">열기</span>}
                </div>
                {card.fields && (
                  <div className="mt-4 flex flex-wrap gap-2">
                    {card.fields.map((field) => (
                      <span key={field} className="border border-[#e8eaf0] bg-[#f8f9fb] px-2 py-1 text-xs text-[#566173]">
                        {field}
                      </span>
                    ))}
                  </div>
                )}
                <p className="mt-4 text-xs text-[#9aa6b8]">{card.status}</p>
              </div>
            );

            return card.href ? (
              <Link key={card.title} href={card.href}>
                {content}
              </Link>
            ) : (
              <div key={card.title}>{content}</div>
            );
          })}
        </div>
      </div>
    </AdminLayout>
  );
}
