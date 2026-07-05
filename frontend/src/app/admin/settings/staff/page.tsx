'use client';

import { useEffect, useMemo, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import Pagination from '@/components/common/Pagination';
import { formatDate } from '@/lib/format';
import {
  staffService,
  type DepartmentOption,
  type EmploymentStatus,
  type PositionOption,
  type StaffCreateRequest,
  type StaffProfile,
  type StaffUpdateRequest,
  type UserRole,
} from '@/lib/services/staffService';

const PAGE_SIZE = 10;

const ROLE_LABEL: Record<UserRole, string> = {
  MANAGER: '매니저',
  ADMIN: '관리자',
  SUPER_ADMIN: '최고관리자',
};

const EMPLOYMENT_STATUS_LABEL: Record<EmploymentStatus, string> = {
  ACTIVE: '재직',
  ON_LEAVE: '휴직',
  RESIGNED: '퇴사',
};

const ROLE_OPTIONS: UserRole[] = ['MANAGER', 'ADMIN', 'SUPER_ADMIN'];
const EMPLOYMENT_STATUS_OPTIONS: EmploymentStatus[] = ['ACTIVE', 'ON_LEAVE', 'RESIGNED'];

type FormState = {
  email: string;
  password: string;
  name: string;
  role: UserRole;
  employeeNo: string;
  departmentId: string;
  positionId: string;
  employmentStatus: EmploymentStatus;
  joinedAt: string;
  leftAt: string;
  active: boolean;
};

const initialForm: FormState = {
  email: '',
  password: '',
  name: '',
  role: 'MANAGER',
  employeeNo: '',
  departmentId: '',
  positionId: '',
  employmentStatus: 'ACTIVE',
  joinedAt: '',
  leftAt: '',
  active: true,
};

function optionalNumber(value: string) {
  return value ? Number(value) : undefined;
}

function optionalString(value: string) {
  const trimmed = value.trim();
  return trimmed ? trimmed : undefined;
}

export default function AdminStaffPage() {
  const [staff, setStaff] = useState<StaffProfile[]>([]);
  const [departments, setDepartments] = useState<DepartmentOption[]>([]);
  const [positions, setPositions] = useState<PositionOption[]>([]);
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [positionId, setPositionId] = useState('');
  const [employmentStatus, setEmploymentStatus] = useState('');
  const [active, setActive] = useState('');
  const [role, setRole] = useState('');
  const [page, setPage] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [editingStaff, setEditingStaff] = useState<StaffProfile | null>(null);
  const [form, setForm] = useState<FormState>(initialForm);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  const activeDepartments = useMemo(() => departments.filter((item) => item.active), [departments]);
  const activePositions = useMemo(() => positions.filter((item) => item.active), [positions]);

  useEffect(() => {
    let mounted = true;
    Promise.all([staffService.getDepartments(), staffService.getPositions()])
      .then(([departmentData, positionData]) => {
        if (!mounted) return;
        setDepartments(departmentData);
        setPositions(positionData);
      })
      .catch(() => {
        if (!mounted) return;
        setDepartments([]);
        setPositions([]);
      });
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    let mounted = true;
    const loadStaff = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await staffService.getStaff({
          keyword: searchKeyword || undefined,
          departmentId: optionalNumber(departmentId),
          positionId: optionalNumber(positionId),
          employmentStatus: employmentStatus ? (employmentStatus as EmploymentStatus) : undefined,
          active: active === '' ? undefined : active === 'true',
          role: role ? (role as UserRole) : undefined,
          page: page - 1,
          size: PAGE_SIZE,
        });
        if (!mounted) return;
        setStaff(response.content);
        setTotalElements(response.totalElements);
        setTotalPages(response.totalPages || 1);
      } catch (err) {
        if (!mounted) return;
        setStaff([]);
        setTotalElements(0);
        setTotalPages(1);
        setError(err instanceof Error ? err.message : '직원 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadStaff();
    return () => {
      mounted = false;
    };
  }, [active, departmentId, employmentStatus, page, positionId, reloadKey, role, searchKeyword]);

  const resetForm = () => {
    setEditingStaff(null);
    setForm(initialForm);
    setFormError('');
  };

  const editStaff = (item: StaffProfile) => {
    setEditingStaff(item);
    setForm({
      email: item.userEmail,
      password: '',
      name: item.userName,
      role: item.userRole,
      employeeNo: item.employeeNo ?? '',
      departmentId: item.departmentId ? String(item.departmentId) : '',
      positionId: item.positionId ? String(item.positionId) : '',
      employmentStatus: item.employmentStatus,
      joinedAt: item.joinedAt ?? '',
      leftAt: item.leftAt ?? '',
      active: item.active,
    });
    setFormError('');
  };

  const set = <K extends keyof FormState>(field: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const validateForm = () => {
    if (!editingStaff && !form.email.trim()) return '이메일을 입력해주세요.';
    if (!editingStaff && form.password.length < 8) return '임시 비밀번호는 8자 이상이어야 합니다.';
    if (!form.name.trim()) return '이름을 입력해주세요.';
    return '';
  };

  const submitForm = async (event: React.FormEvent) => {
    event.preventDefault();
    const message = validateForm();
    if (message) {
      setFormError(message);
      return;
    }

    setSubmitting(true);
    setFormError('');
    try {
      if (editingStaff) {
        const payload: StaffUpdateRequest = {
          name: form.name.trim(),
          role: form.role,
          employeeNo: optionalString(form.employeeNo),
          departmentId: optionalNumber(form.departmentId),
          positionId: optionalNumber(form.positionId),
          employmentStatus: form.employmentStatus,
          joinedAt: optionalString(form.joinedAt),
          leftAt: optionalString(form.leftAt),
          active: form.active,
        };
        await staffService.updateStaff(editingStaff.id, payload);
      } else {
        const payload: StaffCreateRequest = {
          email: form.email.trim(),
          password: form.password,
          name: form.name.trim(),
          role: form.role,
          employeeNo: optionalString(form.employeeNo),
          departmentId: optionalNumber(form.departmentId),
          positionId: optionalNumber(form.positionId),
          employmentStatus: form.employmentStatus,
          joinedAt: optionalString(form.joinedAt),
          active: form.active,
        };
        await staffService.createStaff(payload);
      }
      resetForm();
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setFormError(err instanceof Error ? err.message : '직원 정보를 저장하지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSearch = () => {
    setSearchKeyword(keyword);
    setPage(1);
  };

  const changeEmploymentStatus = async (item: StaffProfile, nextStatus: EmploymentStatus) => {
    if (!confirm(`재직 상태를 "${EMPLOYMENT_STATUS_LABEL[nextStatus]}"로 변경하시겠습니까?`)) return;
    try {
      await staffService.updateEmploymentStatus(item.id, nextStatus);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '재직 상태 변경에 실패했습니다.');
    }
  };

  const changeActive = async (item: StaffProfile) => {
    const nextActive = !item.active;
    if (!confirm(`직원 프로필을 ${nextActive ? '활성' : '비활성'} 처리하시겠습니까?`)) return;
    try {
      await staffService.updateActive(item.id, nextActive);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '활성 상태 변경에 실패했습니다.');
    }
  };

  return (
    <AdminLayout title="직원 관리">
      <div className="space-y-5">
        <div className="border border-[#e8eaf0] bg-white p-5">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="text-base font-semibold text-[#1a1f2e]">직원 계정과 인사 프로필</h2>
              <p className="mt-2 text-sm text-[#6f7a8a]">
                직원 계정은 기존 User 계정과 연결되며, 권한 그룹과 세부 권한은 v0.4.3 이후 단계에서 확장됩니다.
              </p>
            </div>
            <p className="text-sm text-[#8a9bb5]">
              총 <span className="font-semibold text-[#1a1f2e]">{totalElements}</span>명
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_380px] gap-5">
          <div className="space-y-4">
            <div className="border border-[#e8eaf0] bg-white p-4 space-y-3">
              <div className="flex gap-3">
                <input
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                  onKeyDown={(event) => event.key === 'Enter' && handleSearch()}
                  placeholder="이름, 이메일, 사번 검색"
                  className="flex-1 border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
                />
                <Button variant="primary" size="sm" onClick={handleSearch}>검색</Button>
              </div>
              <div className="grid grid-cols-2 lg:grid-cols-5 gap-3">
                <select value={departmentId} onChange={(event) => { setDepartmentId(event.target.value); setPage(1); }} className="border border-[#e0e0e0] bg-white px-3 py-2 text-sm">
                  <option value="">전체 부서</option>
                  {departments.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
                </select>
                <select value={positionId} onChange={(event) => { setPositionId(event.target.value); setPage(1); }} className="border border-[#e0e0e0] bg-white px-3 py-2 text-sm">
                  <option value="">전체 직급</option>
                  {positions.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
                </select>
                <select value={employmentStatus} onChange={(event) => { setEmploymentStatus(event.target.value); setPage(1); }} className="border border-[#e0e0e0] bg-white px-3 py-2 text-sm">
                  <option value="">전체 재직 상태</option>
                  {EMPLOYMENT_STATUS_OPTIONS.map((item) => <option key={item} value={item}>{EMPLOYMENT_STATUS_LABEL[item]}</option>)}
                </select>
                <select value={active} onChange={(event) => { setActive(event.target.value); setPage(1); }} className="border border-[#e0e0e0] bg-white px-3 py-2 text-sm">
                  <option value="">전체 활성 상태</option>
                  <option value="true">활성</option>
                  <option value="false">비활성</option>
                </select>
                <select value={role} onChange={(event) => { setRole(event.target.value); setPage(1); }} className="border border-[#e0e0e0] bg-white px-3 py-2 text-sm">
                  <option value="">전체 역할</option>
                  {ROLE_OPTIONS.map((item) => <option key={item} value={item}>{ROLE_LABEL[item]}</option>)}
                </select>
              </div>
            </div>

            {loading ? (
              <div className="py-12 text-center text-[#bbb] text-sm">로딩 중...</div>
            ) : error ? (
              <div className="bg-white border border-[#f1c7c7] px-5 py-12 text-center">
                <p className="text-sm text-[#c43a3a]">{error}</p>
                <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
                  다시 불러오기
                </Button>
              </div>
            ) : (
              <DataTable<StaffProfile>
                keyField="id"
                data={staff}
                emptyMessage="직원 데이터가 없습니다."
                columns={[
                  { key: 'employeeNo', header: '사번', render: (row) => row.employeeNo ?? '-' },
                  {
                    key: 'userName',
                    header: '직원',
                    render: (row) => (
                      <div>
                        <p className="font-medium text-[#222]">{row.userName}</p>
                        <p className="text-xs text-[#999]">{row.userEmail}</p>
                      </div>
                    ),
                  },
                  { key: 'userRole', header: '역할', render: (row) => ROLE_LABEL[row.userRole] ?? row.userRole },
                  { key: 'departmentName', header: '부서', render: (row) => row.departmentName ?? '-' },
                  { key: 'positionName', header: '직급', render: (row) => row.positionName ?? '-' },
                  {
                    key: 'employmentStatus',
                    header: '재직 상태',
                    render: (row) => (
                      <select
                        value={row.employmentStatus}
                        onChange={(event) => changeEmploymentStatus(row, event.target.value as EmploymentStatus)}
                        className="border border-[#e0e0e0] bg-white px-2 py-1 text-xs"
                      >
                        {EMPLOYMENT_STATUS_OPTIONS.map((item) => (
                          <option key={item} value={item}>{EMPLOYMENT_STATUS_LABEL[item]}</option>
                        ))}
                      </select>
                    ),
                  },
                  {
                    key: 'active',
                    header: '활성',
                    render: (row) => (
                      <button
                        type="button"
                        onClick={() => changeActive(row)}
                        className={`px-2 py-1 text-xs font-medium ${row.active ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500'}`}
                      >
                        {row.active ? '활성' : '비활성'}
                      </button>
                    ),
                  },
                  { key: 'joinedAt', header: '입사일', render: (row) => row.joinedAt ? formatDate(row.joinedAt) : '-' },
                  {
                    key: 'actions',
                    header: '관리',
                    render: (row) => (
                      <Button variant="outline" size="sm" onClick={() => editStaff(row)}>수정</Button>
                    ),
                  },
                ]}
              />
            )}

            {!loading && !error && (
              <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            )}
          </div>

          <form onSubmit={submitForm} className="border border-[#e8eaf0] bg-white p-5 space-y-4 h-fit">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-[#1a1f2e]">{editingStaff ? '직원 수정' : '직원 등록'}</h3>
              {editingStaff && <Button type="button" variant="ghost" size="sm" onClick={resetForm}>신규 등록</Button>}
            </div>

            <Input label="이메일" type="email" value={form.email} disabled={Boolean(editingStaff)} onChange={(event) => set('email', event.target.value)} fullWidth />
            {!editingStaff && <Input label="임시 비밀번호" type="password" value={form.password} onChange={(event) => set('password', event.target.value)} placeholder="8자 이상" fullWidth />}
            <Input label="이름" value={form.name} onChange={(event) => set('name', event.target.value)} fullWidth />
            <Input label="사번" value={form.employeeNo} onChange={(event) => set('employeeNo', event.target.value)} fullWidth />

            <div>
              <label className="block text-sm font-medium text-[#444] mb-1">역할</label>
              <select value={form.role} onChange={(event) => set('role', event.target.value as UserRole)} className="w-full border border-[#ddd] bg-white px-3 py-2.5 text-sm">
                {ROLE_OPTIONS.map((item) => <option key={item} value={item}>{ROLE_LABEL[item]}</option>)}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-[#444] mb-1">부서</label>
                <select value={form.departmentId} onChange={(event) => set('departmentId', event.target.value)} className="w-full border border-[#ddd] bg-white px-3 py-2.5 text-sm">
                  <option value="">미지정</option>
                  {activeDepartments.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-[#444] mb-1">직급</label>
                <select value={form.positionId} onChange={(event) => set('positionId', event.target.value)} className="w-full border border-[#ddd] bg-white px-3 py-2.5 text-sm">
                  <option value="">미지정</option>
                  {activePositions.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-[#444] mb-1">재직 상태</label>
              <select value={form.employmentStatus} onChange={(event) => set('employmentStatus', event.target.value as EmploymentStatus)} className="w-full border border-[#ddd] bg-white px-3 py-2.5 text-sm">
                {EMPLOYMENT_STATUS_OPTIONS.map((item) => <option key={item} value={item}>{EMPLOYMENT_STATUS_LABEL[item]}</option>)}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <Input label="입사일" type="date" value={form.joinedAt} onChange={(event) => set('joinedAt', event.target.value)} fullWidth />
              <Input label="퇴사일" type="date" value={form.leftAt} onChange={(event) => set('leftAt', event.target.value)} disabled={form.employmentStatus !== 'RESIGNED'} fullWidth />
            </div>

            <label className="flex items-center gap-2 text-sm text-[#444]">
              <input type="checkbox" checked={form.active} onChange={(event) => set('active', event.target.checked)} />
              직원 프로필 활성
            </label>

            {formError && <p className="text-sm text-[#c43a3a]">{formError}</p>}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={resetForm}>취소</Button>
              <Button type="submit" variant="primary" disabled={submitting}>{submitting ? '저장 중...' : '저장'}</Button>
            </div>
          </form>
        </div>
      </div>
    </AdminLayout>
  );
}
