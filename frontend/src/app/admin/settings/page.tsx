'use client';

import Link from 'next/link';
import { Suspense, useCallback, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import AdminLayout from '@/components/admin/AdminLayout';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import {
  settingsService,
  TERMS_TYPE_LABEL,
  type BusinessSettingsUpdateRequest,
  type TermsType,
  type TermsVersion,
  type TermsVersionCreateRequest,
} from '@/lib/services/settingsService';

type Section = 'company' | 'terms' | 'privacy' | 'policies';

const SECTIONS: Array<{ key: Section; label: string; description: string; termsType?: TermsType }> = [
  { key: 'company', label: '사업자 설정', description: '쇼핑몰 하단과 운영 문서에 사용할 사업자 정보를 저장합니다.' },
  { key: 'terms', label: '이용약관', description: '이용약관 새 버전과 과거 버전을 관리합니다.', termsType: 'TERMS_OF_SERVICE' },
  { key: 'privacy', label: '개인정보처리방침', description: '개인정보처리방침 새 버전과 과거 버전을 관리합니다.', termsType: 'PRIVACY_POLICY' },
  { key: 'policies', label: '배송/반품 정책', description: '배송/반품 정책 새 버전과 과거 버전을 관리합니다.', termsType: 'SHIPPING_RETURN_POLICY' },
];

const EMPTY_COMPANY: BusinessSettingsUpdateRequest = {
  companyName: '',
  representativeName: '',
  businessRegistrationNumber: '',
  mailOrderBusinessNumber: '',
  address: '',
  customerServicePhone: '',
  customerServiceEmail: '',
  brandName: '',
};

function AdminSettingsContent() {
  const searchParams = useSearchParams();
  const sectionParam = searchParams.get('section') as Section | null;
  const activeSection = SECTIONS.some((section) => section.key === sectionParam) ? sectionParam! : 'company';
  const activeConfig = SECTIONS.find((section) => section.key === activeSection)!;

  const [companyForm, setCompanyForm] = useState<BusinessSettingsUpdateRequest>(EMPTY_COMPANY);
  const [termsForm, setTermsForm] = useState<TermsVersionCreateRequest>({
    type: activeConfig.termsType ?? 'TERMS_OF_SERVICE',
    title: '',
    content: '',
    version: '',
    effectiveFrom: '',
  });
  const [latestTerms, setLatestTerms] = useState<TermsVersion | null>(null);
  const [versions, setVersions] = useState<TermsVersion[]>([]);
  const [selectedVersion, setSelectedVersion] = useState<TermsVersion | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const termsType = activeConfig.termsType;
  const isCompany = activeSection === 'company';

  const loadSection = useCallback(async (section: Section, currentTermsType?: TermsType) => {
    setError('');
    setMessage('');
    setSelectedVersion(null);
    if (currentTermsType) {
      setTermsForm({ type: currentTermsType, title: '', content: '', version: '', effectiveFrom: '' });
    }
    setLoading(true);
    try {
      if (section === 'company') {
        const data = await settingsService.getCompanySettings();
        setCompanyForm({
          companyName: data.companyName ?? '',
          representativeName: data.representativeName ?? '',
          businessRegistrationNumber: data.businessRegistrationNumber ?? '',
          mailOrderBusinessNumber: data.mailOrderBusinessNumber ?? '',
          address: data.address ?? '',
          customerServicePhone: data.customerServicePhone ?? '',
          customerServiceEmail: data.customerServiceEmail ?? '',
          brandName: data.brandName ?? '',
        });
      } else if (currentTermsType) {
        const rows = await settingsService.getTermsVersions(currentTermsType);
        setVersions(rows);
        setLatestTerms(rows.find((row) => row.active) ?? rows[0] ?? null);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '설정 정보를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadSection(activeSection, termsType);
    }, 0);
    return () => window.clearTimeout(timer);
  }, [activeSection, termsType, loadSection]);

  const versionRows = useMemo(() => versions.slice(0, 10), [versions]);

  async function saveCompany() {
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const saved = await settingsService.updateCompanySettings(companyForm);
      setCompanyForm({
        companyName: saved.companyName ?? '',
        representativeName: saved.representativeName ?? '',
        businessRegistrationNumber: saved.businessRegistrationNumber ?? '',
        mailOrderBusinessNumber: saved.mailOrderBusinessNumber ?? '',
        address: saved.address ?? '',
        customerServicePhone: saved.customerServicePhone ?? '',
        customerServiceEmail: saved.customerServiceEmail ?? '',
        brandName: saved.brandName ?? '',
      });
      setMessage('사업자 설정이 저장되었습니다.');
    } catch (err) {
      setError(err instanceof Error ? err.message : '사업자 설정 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  }

  async function saveTerms() {
    if (!termsType) return;
    setSaving(true);
    setError('');
    setMessage('');
    try {
      await settingsService.createTerms({
        ...termsForm,
        type: termsType,
        effectiveFrom: termsForm.effectiveFrom || undefined,
        version: termsForm.version || undefined,
      });
      setTermsForm({ type: termsType, title: '', content: '', version: '', effectiveFrom: '' });
      setMessage(`${TERMS_TYPE_LABEL[termsType]} 새 버전이 저장되었습니다.`);
      await loadSection(activeSection, termsType);
    } catch (err) {
      setError(err instanceof Error ? err.message : '약관/정책 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <AdminLayout title="시스템 설정">
      <div className="space-y-5">
        <div className="border border-[#e8eaf0] bg-white p-5">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h2 className="text-base font-semibold text-[#1a1f2e]">설정 저장 관리</h2>
              <p className="mt-2 text-sm text-[#6f7a8a]">
                사업자 정보와 약관/정책 문서를 DB 기준으로 저장하고, 약관/정책은 새 버전 생성 방식으로 이력을 보관합니다.
              </p>
            </div>
            <div className="flex flex-wrap gap-2 text-sm">
              <Link className="text-[#4c74e5]" href="/admin/settings/audit-logs">관리자 작업 이력</Link>
              <Link className="text-[#4c74e5]" href="/admin/settings/staff">직원 관리</Link>
              <Link className="text-[#4c74e5]" href="/admin/settings/permission-groups">권한 그룹</Link>
            </div>
          </div>
        </div>

        <div className="flex flex-wrap gap-2">
          {SECTIONS.map((section) => (
            <Link
              key={section.key}
              href={`/admin/settings?section=${section.key}`}
              className={`border px-3 py-2 text-sm ${
                activeSection === section.key
                  ? 'border-[#1a1f2e] bg-[#1a1f2e] text-white'
                  : 'border-[#e8eaf0] bg-white text-[#566173]'
              }`}
            >
              {section.label}
            </Link>
          ))}
        </div>

        {error && <p className="border border-[#f0c7c7] bg-[#fff5f5] p-3 text-sm text-[#c44242]">{error}</p>}
        {message && <p className="border border-[#bfe3cb] bg-[#f3fbf5] p-3 text-sm text-[#2e7d47]">{message}</p>}

        {loading ? (
          <div className="border border-[#e8eaf0] bg-white p-8 text-center text-sm text-[#6f7a8a]">설정 정보를 불러오는 중입니다.</div>
        ) : isCompany ? (
          <section className="border border-[#e8eaf0] bg-white p-5">
            <h3 className="text-sm font-semibold text-[#1a1f2e]">{activeConfig.label}</h3>
            <p className="mt-2 text-sm text-[#6f7a8a]">{activeConfig.description}</p>
            <div className="mt-5 grid grid-cols-1 gap-4 lg:grid-cols-2">
              <Input label="상호명" value={companyForm.companyName} onChange={(e) => setCompanyForm({ ...companyForm, companyName: e.target.value })} />
              <Input label="브랜드명" value={companyForm.brandName} onChange={(e) => setCompanyForm({ ...companyForm, brandName: e.target.value })} />
              <Input label="대표자명" value={companyForm.representativeName} onChange={(e) => setCompanyForm({ ...companyForm, representativeName: e.target.value })} />
              <Input label="사업자등록번호" value={companyForm.businessRegistrationNumber} onChange={(e) => setCompanyForm({ ...companyForm, businessRegistrationNumber: e.target.value })} />
              <Input label="통신판매업 신고번호" value={companyForm.mailOrderBusinessNumber} onChange={(e) => setCompanyForm({ ...companyForm, mailOrderBusinessNumber: e.target.value })} />
              <Input label="고객센터 전화번호" value={companyForm.customerServicePhone} onChange={(e) => setCompanyForm({ ...companyForm, customerServicePhone: e.target.value })} />
              <Input label="고객센터 이메일" value={companyForm.customerServiceEmail} onChange={(e) => setCompanyForm({ ...companyForm, customerServiceEmail: e.target.value })} />
              <Input label="주소" value={companyForm.address} onChange={(e) => setCompanyForm({ ...companyForm, address: e.target.value })} />
            </div>
            <div className="mt-5 flex justify-end">
              <Button onClick={saveCompany} disabled={saving}>{saving ? '저장 중' : '저장'}</Button>
            </div>
          </section>
        ) : termsType ? (
          <section className="grid grid-cols-1 gap-4 xl:grid-cols-[1.2fr_0.8fr]">
            <div className="border border-[#e8eaf0] bg-white p-5">
              <h3 className="text-sm font-semibold text-[#1a1f2e]">{activeConfig.label}</h3>
              <p className="mt-2 text-sm text-[#6f7a8a]">{activeConfig.description}</p>
              <div className="mt-5 space-y-4">
                <Input label="제목" value={termsForm.title ?? ''} onChange={(e) => setTermsForm({ ...termsForm, title: e.target.value })} />
                <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
                  <Input label="버전명" placeholder="비워두면 자동 생성" value={termsForm.version ?? ''} onChange={(e) => setTermsForm({ ...termsForm, version: e.target.value })} />
                  <Input label="적용 시작일" type="datetime-local" value={termsForm.effectiveFrom ?? ''} onChange={(e) => setTermsForm({ ...termsForm, effectiveFrom: e.target.value })} />
                </div>
                <label className="block text-sm text-[#424b5f]">
                  내용
                  <textarea
                    className="mt-2 min-h-[260px] w-full border border-[#d9deea] p-3 text-sm outline-none focus:border-[#4c74e5]"
                    value={termsForm.content ?? ''}
                    onChange={(e) => setTermsForm({ ...termsForm, content: e.target.value })}
                  />
                </label>
              </div>
              <div className="mt-5 flex justify-end">
                <Button onClick={saveTerms} disabled={saving}>{saving ? '저장 중' : '새 버전 저장'}</Button>
              </div>
            </div>

            <div className="space-y-4">
              <div className="border border-[#e8eaf0] bg-white p-5">
                <h4 className="text-sm font-semibold text-[#1a1f2e]">최신 버전</h4>
                {latestTerms ? (
                  <div className="mt-3 text-sm text-[#566173]">
                    <p className="font-medium text-[#1a1f2e]">{latestTerms.title}</p>
                    <p className="mt-1">버전: {latestTerms.version}</p>
                    <p className="mt-1">적용일: {formatDateTime(latestTerms.effectiveFrom)}</p>
                  </div>
                ) : (
                  <p className="mt-3 text-sm text-[#8b95a5]">저장된 버전이 없습니다.</p>
                )}
              </div>

              <div className="border border-[#e8eaf0] bg-white p-5">
                <h4 className="text-sm font-semibold text-[#1a1f2e]">버전 이력</h4>
                <div className="mt-3 space-y-2">
                  {versionRows.length === 0 ? (
                    <p className="text-sm text-[#8b95a5]">버전 이력이 없습니다.</p>
                  ) : (
                    versionRows.map((version) => (
                      <button
                        key={version.id}
                        type="button"
                        onClick={() => setSelectedVersion(version)}
                        className="w-full border border-[#e8eaf0] p-3 text-left text-sm hover:border-[#cfd6e6]"
                      >
                        <span className="font-medium text-[#1a1f2e]">{version.version}</span>
                        <span className="ml-2 text-[#6f7a8a]">{version.title}</span>
                        {version.active && <span className="ml-2 text-[#2e7d47]">최신</span>}
                      </button>
                    ))
                  )}
                </div>
              </div>

              {selectedVersion && (
                <div className="border border-[#e8eaf0] bg-white p-5">
                  <h4 className="text-sm font-semibold text-[#1a1f2e]">과거 버전 상세</h4>
                  <p className="mt-2 text-sm font-medium text-[#1a1f2e]">{selectedVersion.title}</p>
                  <p className="mt-1 text-xs text-[#6f7a8a]">{selectedVersion.version} / {formatDateTime(selectedVersion.effectiveFrom)}</p>
                  <pre className="mt-3 max-h-[240px] overflow-auto whitespace-pre-wrap border border-[#edf0f5] bg-[#f8f9fb] p-3 text-xs text-[#424b5f]">
                    {selectedVersion.content}
                  </pre>
                </div>
              )}
            </div>
          </section>
        ) : null}
      </div>
    </AdminLayout>
  );
}

export default function AdminSettingsPage() {
  return (
    <Suspense fallback={<div className="p-6 text-sm text-[#6f7a8a]">설정 정보를 불러오는 중입니다.</div>}>
      <AdminSettingsContent />
    </Suspense>
  );
}

function formatDateTime(value: string | null | undefined) {
  if (!value) return '-';
  return new Date(value).toLocaleString('ko-KR');
}
