import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';

const ROLE_POLICIES = [
  {
    role: 'SUPER_ADMIN',
    label: '최고관리자',
    description: '직원, 권한 그룹, 시스템 설정을 포함한 전체 운영 권한을 갖습니다.',
    next: 'v0.4.4 이후 모든 permission code를 보유한 역할로 매핑합니다.',
  },
  {
    role: 'ADMIN',
    label: '관리자',
    description: '상품, 주문, 고객, 운영 관리 변경 권한을 중심으로 운영합니다.',
    next: '쓰기/상태 변경 중심 permission group과 병행합니다.',
  },
  {
    role: 'MANAGER',
    label: '매니저',
    description: '주요 운영 화면 조회 중심 접근을 제공합니다.',
    next: '조회 전용 permission group과 병행합니다.',
  },
  {
    role: 'USER',
    label: '일반 사용자',
    description: '쇼핑몰 회원 권한이며 관리자 권한 그룹 할당 대상에서 제외합니다.',
    next: '일반 사용자 권한 그룹 고도화는 v0.4 범위에서 제외합니다.',
  },
];

export default function AdminRolesPage() {
  return (
    <AdminLayout title="역할/권한 설정">
      <div className="space-y-5">
        <div className="border border-[#e8eaf0] bg-white p-5">
          <h2 className="text-base font-semibold text-[#1a1f2e]">기존 역할과 권한 그룹 병행 정책</h2>
          <p className="mt-2 text-sm text-[#6f7a8a] leading-6">
            v0.4.3에서는 기존 role 기반 API 접근 제어를 유지하고, 권한 그룹을 추가로 할당하는 기반만 제공합니다.
            메뉴별/기능별 권한 매트릭스와 API permission 기반 접근 제어는 v0.4.4 ~ v0.4.6에서 단계적으로 연결합니다.
          </p>
          <Link href="/admin/settings/permission-groups" className="mt-4 inline-flex text-sm font-medium text-[#4c74e5] hover:text-[#294fc7]">
            권한 그룹 관리로 이동
          </Link>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {ROLE_POLICIES.map((policy) => (
            <section key={policy.role} className="border border-[#e8eaf0] bg-white p-5">
              <div className="flex items-center justify-between gap-3">
                <h3 className="text-sm font-semibold text-[#1a1f2e]">{policy.label}</h3>
                <span className="text-xs text-[#8a9bb5]">{policy.role}</span>
              </div>
              <p className="mt-3 text-sm text-[#566173] leading-6">{policy.description}</p>
              <p className="mt-4 text-xs text-[#8a9bb5]">후속 기준: {policy.next}</p>
            </section>
          ))}
        </div>
      </div>
    </AdminLayout>
  );
}
