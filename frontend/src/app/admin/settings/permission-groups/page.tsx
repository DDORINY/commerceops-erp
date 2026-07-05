'use client';

import { useEffect, useMemo, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import { formatDate } from '@/lib/format';
import {
  permissionGroupService,
  type PermissionGroup,
  type PermissionGroupCreateRequest,
  type PermissionGroupUpdateRequest,
} from '@/lib/services/permissionGroupService';
import { staffService, type StaffProfile } from '@/lib/services/staffService';

const PAGE_SIZE_FOR_ASSIGNMENT = 100;

type GroupForm = {
  name: string;
  code: string;
  description: string;
  active: boolean;
};

const initialForm: GroupForm = {
  name: '',
  code: '',
  description: '',
  active: true,
};

function normalizeCode(value: string) {
  return value.trim().toUpperCase().replace(/[^A-Z0-9_]+/g, '_');
}

export default function PermissionGroupsPage() {
  const [groups, setGroups] = useState<PermissionGroup[]>([]);
  const [staff, setStaff] = useState<StaffProfile[]>([]);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedGroupIds, setSelectedGroupIds] = useState<number[]>([]);
  const [editingGroup, setEditingGroup] = useState<PermissionGroup | null>(null);
  const [form, setForm] = useState<GroupForm>(initialForm);
  const [loading, setLoading] = useState(true);
  const [assignmentLoading, setAssignmentLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [formError, setFormError] = useState('');
  const [assignmentMessage, setAssignmentMessage] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const activeGroups = useMemo(() => groups.filter((group) => group.active), [groups]);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [groupData, staffData] = await Promise.all([
          permissionGroupService.getPermissionGroups(),
          staffService.getStaff({ page: 0, size: PAGE_SIZE_FOR_ASSIGNMENT }),
        ]);
        if (!mounted) return;
        setGroups(groupData);
        setStaff(staffData.content);
      } catch (err) {
        if (!mounted) return;
        setGroups([]);
        setStaff([]);
        setError(err instanceof Error ? err.message : '권한 그룹 정보를 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => {
      mounted = false;
    };
  }, [reloadKey]);

  useEffect(() => {
    let mounted = true;
    const loadAssignments = async () => {
      if (!selectedUserId) {
        setSelectedGroupIds([]);
        return;
      }
      setAssignmentLoading(true);
      setAssignmentMessage('');
      try {
        const assignments = await permissionGroupService.getUserPermissionGroups(Number(selectedUserId));
        if (!mounted) return;
        setSelectedGroupIds(assignments.map((assignment) => assignment.permissionGroupId));
      } catch (err) {
        if (!mounted) return;
        setSelectedGroupIds([]);
        setAssignmentMessage(err instanceof Error ? err.message : '직원 권한 그룹을 불러오지 못했습니다.');
      } finally {
        if (mounted) setAssignmentLoading(false);
      }
    };
    loadAssignments();
    return () => {
      mounted = false;
    };
  }, [selectedUserId]);

  const resetForm = () => {
    setEditingGroup(null);
    setForm(initialForm);
    setFormError('');
  };

  const editGroup = (group: PermissionGroup) => {
    setEditingGroup(group);
    setForm({
      name: group.name,
      code: group.code,
      description: group.description ?? '',
      active: group.active,
    });
    setFormError('');
  };

  const submitForm = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!form.name.trim()) {
      setFormError('권한 그룹명을 입력해주세요.');
      return;
    }
    if (!editingGroup && !form.code.trim()) {
      setFormError('권한 그룹 코드를 입력해주세요.');
      return;
    }

    setSubmitting(true);
    setFormError('');
    try {
      if (editingGroup) {
        const payload: PermissionGroupUpdateRequest = {
          name: form.name.trim(),
          description: form.description.trim() || undefined,
        };
        await permissionGroupService.updatePermissionGroup(editingGroup.id, payload);
      } else {
        const payload: PermissionGroupCreateRequest = {
          name: form.name.trim(),
          code: normalizeCode(form.code),
          description: form.description.trim() || undefined,
          active: form.active,
        };
        await permissionGroupService.createPermissionGroup(payload);
      }
      resetForm();
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      setFormError(err instanceof Error ? err.message : '권한 그룹을 저장하지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const changeActive = async (group: PermissionGroup) => {
    if (group.systemGroup && group.active) {
      alert('시스템 권한 그룹은 비활성화할 수 없습니다.');
      return;
    }
    const nextActive = !group.active;
    if (!confirm(`권한 그룹을 ${nextActive ? '활성' : '비활성'} 처리하시겠습니까?`)) return;
    try {
      await permissionGroupService.updatePermissionGroupActive(group.id, nextActive);
      setReloadKey((prev) => prev + 1);
    } catch (err) {
      alert(err instanceof Error ? err.message : '권한 그룹 활성 상태를 변경하지 못했습니다.');
    }
  };

  const toggleAssignment = (groupId: number) => {
    setSelectedGroupIds((prev) =>
      prev.includes(groupId) ? prev.filter((id) => id !== groupId) : [...prev, groupId]
    );
  };

  const saveAssignments = async () => {
    if (!selectedUserId) {
      setAssignmentMessage('직원을 선택해주세요.');
      return;
    }
    setAssignmentLoading(true);
    setAssignmentMessage('');
    try {
      await permissionGroupService.updateUserPermissionGroups(Number(selectedUserId), selectedGroupIds);
      setAssignmentMessage('직원 권한 그룹을 저장했습니다.');
    } catch (err) {
      setAssignmentMessage(err instanceof Error ? err.message : '직원 권한 그룹 저장에 실패했습니다.');
    } finally {
      setAssignmentLoading(false);
    }
  };

  return (
    <AdminLayout title="권한 그룹 관리">
      <div className="space-y-5">
        <div className="border border-[#e8eaf0] bg-white p-5">
          <h2 className="text-base font-semibold text-[#1a1f2e]">권한 그룹과 기존 역할 병행 운영</h2>
          <p className="mt-2 text-sm text-[#6f7a8a] leading-6">
            v0.4.3에서는 기존 USER/MANAGER/ADMIN/SUPER_ADMIN 역할을 유지하면서 권한 그룹을 추가로 할당합니다.
            실제 메뉴별/기능별 권한 매트릭스와 API permission 기반 제어는 v0.4.4 이후 단계에서 연결합니다.
          </p>
        </div>

        <div className="grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_390px] gap-5">
          <div className="space-y-4">
            {loading ? (
              <div className="py-12 text-center text-[#bbb] text-sm">권한 그룹을 불러오는 중...</div>
            ) : error ? (
              <div className="bg-white border border-[#f1c7c7] px-5 py-12 text-center">
                <p className="text-sm text-[#c43a3a]">{error}</p>
                <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
                  다시 불러오기
                </Button>
              </div>
            ) : (
              <DataTable<PermissionGroup>
                keyField="id"
                data={groups}
                emptyMessage="권한 그룹이 없습니다."
                columns={[
                  { key: 'name', header: '그룹명' },
                  { key: 'code', header: '코드' },
                  {
                    key: 'systemGroup',
                    header: '구분',
                    render: (row) => row.systemGroup ? '시스템' : '사용자 정의',
                  },
                  {
                    key: 'activeStatus',
                    header: '상태',
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
                  { key: 'createdAt', header: '생성일', render: (row) => formatDate(row.createdAt) },
                  {
                    key: 'actions',
                    header: '관리',
                    render: (row) => (
                      <Button variant="outline" size="sm" onClick={() => editGroup(row)}>수정</Button>
                    ),
                  },
                ]}
              />
            )}

            <div className="border border-[#e8eaf0] bg-white p-5 space-y-4">
              <div>
                <h3 className="text-sm font-semibold text-[#1a1f2e]">직원별 권한 그룹 할당</h3>
                <p className="mt-1 text-sm text-[#6f7a8a]">v0.4.3에서는 관리자 계열 직원에게만 권한 그룹을 할당합니다.</p>
              </div>
              <select
                value={selectedUserId}
                onChange={(event) => setSelectedUserId(event.target.value)}
                className="w-full border border-[#ddd] bg-white px-3 py-2.5 text-sm"
              >
                <option value="">직원 선택</option>
                {staff.map((item) => (
                  <option key={item.userId} value={item.userId}>
                    {item.userName} / {item.userEmail} / {item.userRole}
                  </option>
                ))}
              </select>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                {activeGroups.map((group) => (
                  <label key={group.id} className="flex items-center gap-2 border border-[#e8eaf0] px-3 py-2 text-sm text-[#444]">
                    <input
                      type="checkbox"
                      checked={selectedGroupIds.includes(group.id)}
                      onChange={() => toggleAssignment(group.id)}
                      disabled={!selectedUserId || assignmentLoading}
                    />
                    <span>{group.name}</span>
                    {group.systemGroup && <span className="ml-auto text-[10px] text-[#8a9bb5]">시스템</span>}
                  </label>
                ))}
              </div>

              {assignmentMessage && <p className="text-sm text-[#4c5f7a]">{assignmentMessage}</p>}
              <div className="flex justify-end">
                <Button variant="primary" onClick={saveAssignments} disabled={!selectedUserId || assignmentLoading}>
                  {assignmentLoading ? '저장 중...' : '할당 저장'}
                </Button>
              </div>
            </div>
          </div>

          <form onSubmit={submitForm} className="border border-[#e8eaf0] bg-white p-5 space-y-4 h-fit">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-[#1a1f2e]">{editingGroup ? '권한 그룹 수정' : '권한 그룹 생성'}</h3>
              {editingGroup && <Button type="button" variant="ghost" size="sm" onClick={resetForm}>새로 생성</Button>}
            </div>

            <Input label="그룹명" value={form.name} onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))} fullWidth />
            <Input
              label="코드"
              value={form.code}
              onChange={(event) => setForm((prev) => ({ ...prev, code: event.target.value }))}
              disabled={Boolean(editingGroup)}
              placeholder="예: PRODUCT_OPERATOR"
              fullWidth
            />
            <div>
              <label className="block text-sm font-medium text-[#444] mb-1">설명</label>
              <textarea
                value={form.description}
                onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
                className="w-full min-h-28 border border-[#ddd] px-3 py-2.5 text-sm outline-none focus:border-[#1a1f2e]"
              />
            </div>
            {!editingGroup && (
              <label className="flex items-center gap-2 text-sm text-[#444]">
                <input
                  type="checkbox"
                  checked={form.active}
                  onChange={(event) => setForm((prev) => ({ ...prev, active: event.target.checked }))}
                />
                생성 즉시 활성화
              </label>
            )}
            {editingGroup?.systemGroup && (
              <p className="text-xs text-[#8a9bb5]">시스템 그룹은 코드와 시스템 여부를 변경하지 않습니다.</p>
            )}
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
