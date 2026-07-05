'use client';

import { useEffect, useMemo, useState } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Button from '@/components/common/Button';
import {
  permissionGroupService,
  type AdminMenuPermission,
  type Permission,
  type PermissionGroup,
} from '@/lib/services/permissionGroupService';

const DOMAIN_LABEL: Record<string, string> = {
  DASHBOARD: '대시보드',
  PRODUCT: '상품',
  CATEGORY: '카테고리',
  BANNER: '배너',
  ORDER: '주문',
  PAYMENT: '결제/환불',
  INVENTORY: '재고',
  WAREHOUSE: '창고',
  ACCOUNTING: '회계',
  COUPON: '쿠폰',
  REVIEW: '리뷰',
  INQUIRY: '문의',
  SETTINGS: '설정',
  HR: '인사/권한',
  AUDIT: '감사 로그',
};

export default function MenuPermissionsPage() {
  const [groups, setGroups] = useState<PermissionGroup[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [menuPermissions, setMenuPermissions] = useState<AdminMenuPermission[]>([]);
  const [selectedGroupId, setSelectedGroupId] = useState('');
  const [selectedPermissionIds, setSelectedPermissionIds] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [savingMatrix, setSavingMatrix] = useState(false);
  const [savingMenu, setSavingMenu] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const permissionsByDomain = useMemo(() => {
    return permissions.reduce<Record<string, Permission[]>>((acc, permission) => {
      const key = permission.domain || 'OTHER';
      acc[key] = [...(acc[key] ?? []), permission];
      return acc;
    }, {});
  }, [permissions]);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [groupData, permissionData, menuData] = await Promise.all([
          permissionGroupService.getPermissionGroups(),
          permissionGroupService.getPermissions(),
          permissionGroupService.getMenuPermissions(),
        ]);
        if (!mounted) return;
        setGroups(groupData);
        setPermissions(permissionData);
        setMenuPermissions(menuData);
        const firstActiveGroup = groupData.find((group) => group.active);
        if (firstActiveGroup) setSelectedGroupId(String(firstActiveGroup.id));
      } catch (err) {
        if (!mounted) return;
        setError(err instanceof Error ? err.message : '권한 매트릭스를 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    let mounted = true;
    const loadGroupPermissions = async () => {
      if (!selectedGroupId) {
        setSelectedPermissionIds([]);
        return;
      }
      setMessage('');
      try {
        const groupPermissions = await permissionGroupService.getGroupPermissions(Number(selectedGroupId));
        if (!mounted) return;
        setSelectedPermissionIds(groupPermissions.map((permission) => permission.id));
      } catch (err) {
        if (!mounted) return;
        setSelectedPermissionIds([]);
        setMessage(err instanceof Error ? err.message : '권한 그룹별 권한을 불러오지 못했습니다.');
      }
    };
    loadGroupPermissions();
    return () => {
      mounted = false;
    };
  }, [selectedGroupId]);

  const togglePermission = (permissionId: number) => {
    setSelectedPermissionIds((prev) =>
      prev.includes(permissionId) ? prev.filter((id) => id !== permissionId) : [...prev, permissionId]
    );
  };

  const saveMatrix = async () => {
    if (!selectedGroupId) {
      setMessage('권한 그룹을 선택해주세요.');
      return;
    }
    setSavingMatrix(true);
    setMessage('');
    try {
      await permissionGroupService.updateGroupPermissions(Number(selectedGroupId), selectedPermissionIds);
      setMessage('권한 그룹 매트릭스를 저장했습니다.');
    } catch (err) {
      setMessage(err instanceof Error ? err.message : '권한 그룹 매트릭스 저장에 실패했습니다.');
    } finally {
      setSavingMatrix(false);
    }
  };

  const changeMenu = <K extends keyof AdminMenuPermission>(index: number, key: K, value: AdminMenuPermission[K]) => {
    setMenuPermissions((prev) => prev.map((item, currentIndex) => (
      currentIndex === index ? { ...item, [key]: value } : item
    )));
  };

  const saveMenuPermissions = async () => {
    setSavingMenu(true);
    setMessage('');
    try {
      const saved = await permissionGroupService.updateMenuPermissions(menuPermissions.map((item) => ({
        menuKey: item.menuKey,
        menuLabel: item.menuLabel,
        menuPath: item.menuPath,
        requiredPermissionCode: item.requiredPermissionCode,
        visible: item.visible,
        sortOrder: item.sortOrder,
      })));
      setMenuPermissions(saved);
      setMessage('관리자 메뉴 권한을 저장했습니다. 실제 사이드바 반영은 v0.4.5에서 진행합니다.');
    } catch (err) {
      setMessage(err instanceof Error ? err.message : '관리자 메뉴 권한 저장에 실패했습니다.');
    } finally {
      setSavingMenu(false);
    }
  };

  return (
    <AdminLayout title="메뉴/기능 권한 매트릭스">
      <div className="space-y-5">
        <div className="border border-[#e8eaf0] bg-white p-5">
          <h2 className="text-base font-semibold text-[#1a1f2e]">권한 매트릭스 기반</h2>
          <p className="mt-2 text-sm text-[#6f7a8a] leading-6">
            권한 코드와 권한 그룹 매핑, 관리자 메뉴별 필요 권한을 관리합니다. 현재 버전에서는 조회/저장 기반을 만들고,
            실제 사이드바 노출과 API permission 검증은 v0.4.5 ~ v0.4.6에서 연결합니다.
          </p>
        </div>

        {loading ? (
          <div className="py-12 text-center text-[#bbb] text-sm">권한 매트릭스를 불러오는 중...</div>
        ) : error ? (
          <div className="bg-white border border-[#f1c7c7] px-5 py-12 text-center text-sm text-[#c43a3a]">{error}</div>
        ) : (
          <>
            <section className="border border-[#e8eaf0] bg-white p-5 space-y-4">
              <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-3">
                <div>
                  <h3 className="text-sm font-semibold text-[#1a1f2e]">권한 그룹별 기능 권한</h3>
                  <p className="mt-1 text-sm text-[#6f7a8a]">inactive 권한은 신규 매핑할 수 없습니다.</p>
                </div>
                <div className="flex gap-2">
                  <select
                    value={selectedGroupId}
                    onChange={(event) => setSelectedGroupId(event.target.value)}
                    className="min-w-60 border border-[#ddd] bg-white px-3 py-2 text-sm"
                  >
                    <option value="">권한 그룹 선택</option>
                    {groups.map((group) => (
                      <option key={group.id} value={group.id} disabled={!group.active}>
                        {group.name} {group.systemGroup ? '(시스템)' : ''}
                      </option>
                    ))}
                  </select>
                  <Button variant="primary" onClick={saveMatrix} disabled={!selectedGroupId || savingMatrix}>
                    {savingMatrix ? '저장 중...' : '매트릭스 저장'}
                  </Button>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                {Object.entries(permissionsByDomain).map(([domain, items]) => (
                  <div key={domain} className="border border-[#eef1f6] p-4">
                    <h4 className="text-sm font-semibold text-[#1a1f2e] mb-3">{DOMAIN_LABEL[domain] ?? domain}</h4>
                    <div className="space-y-2">
                      {items.map((permission) => (
                        <label key={permission.id} className="flex items-start gap-2 text-sm text-[#444]">
                          <input
                            type="checkbox"
                            className="mt-1"
                            checked={selectedPermissionIds.includes(permission.id)}
                            onChange={() => togglePermission(permission.id)}
                            disabled={!permission.active}
                          />
                          <span>
                            <span className="font-medium text-[#222]">{permission.name}</span>
                            <span className="ml-2 text-xs text-[#8a9bb5]">{permission.code}</span>
                            {permission.description && <span className="block text-xs text-[#8a9bb5] mt-0.5">{permission.description}</span>}
                          </span>
                        </label>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </section>

            <section className="border border-[#e8eaf0] bg-white p-5 space-y-4">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <h3 className="text-sm font-semibold text-[#1a1f2e]">관리자 메뉴별 필요 권한</h3>
                  <p className="mt-1 text-sm text-[#6f7a8a]">v0.4.4에서는 기준만 저장하고, 실제 메뉴 노출은 v0.4.5에서 연결합니다.</p>
                </div>
                <Button variant="primary" onClick={saveMenuPermissions} disabled={savingMenu}>
                  {savingMenu ? '저장 중...' : '메뉴 권한 저장'}
                </Button>
              </div>

              <DataTable<AdminMenuPermission>
                keyField="id"
                data={menuPermissions}
                emptyMessage="관리자 메뉴 권한 데이터가 없습니다."
                columns={[
                  { key: 'menuLabel', header: '메뉴명' },
                  { key: 'menuPath', header: '경로' },
                  {
                    key: 'requiredPermissionCode',
                    header: '필요 권한',
                    render: (row) => {
                      const index = menuPermissions.findIndex((item) => item.id === row.id);
                      return (
                        <select
                          value={row.requiredPermissionCode}
                          onChange={(event) => changeMenu(index, 'requiredPermissionCode', event.target.value)}
                          className="min-w-52 border border-[#ddd] bg-white px-2 py-1 text-xs"
                        >
                          {permissions.filter((permission) => permission.active).map((permission) => (
                            <option key={permission.id} value={permission.code}>{permission.code}</option>
                          ))}
                        </select>
                      );
                    },
                  },
                  {
                    key: 'visibleControl',
                    header: '노출',
                    render: (row) => {
                      const index = menuPermissions.findIndex((item) => item.id === row.id);
                      return (
                        <input
                          type="checkbox"
                          checked={row.visible}
                          onChange={(event) => changeMenu(index, 'visible', event.target.checked)}
                        />
                      );
                    },
                  },
                  {
                    key: 'sortOrderControl',
                    header: '정렬',
                    render: (row) => {
                      const index = menuPermissions.findIndex((item) => item.id === row.id);
                      return (
                        <input
                          type="number"
                          value={row.sortOrder}
                          onChange={(event) => changeMenu(index, 'sortOrder', Number(event.target.value))}
                          className="w-20 border border-[#ddd] px-2 py-1 text-xs"
                        />
                      );
                    },
                  },
                ]}
              />
            </section>
          </>
        )}

        {message && (
          <div className="border border-[#dce6ff] bg-[#f7f9ff] px-4 py-3 text-sm text-[#36527a]">
            {message}
          </div>
        )}
      </div>
    </AdminLayout>
  );
}
