'use client';

import { useState, useEffect } from 'react';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { userService, type ApiUser } from '@/lib/services/userService';
import { formatDate, formatPrice } from '@/lib/format';

const PAGE_SIZE = 10;

const ROLE_LABEL: Record<string, string> = {
  USER: '일반',
  MANAGER: '매니저',
  ADMIN: '관리자',
  SUPER_ADMIN: '최고관리자',
};

const ROLE_COLOR: Record<string, string> = {
  USER: 'bg-blue-50 text-blue-600',
  MANAGER: 'bg-amber-50 text-amber-700',
  ADMIN: 'bg-purple-100 text-purple-700',
  SUPER_ADMIN: 'bg-red-100 text-red-700',
};

const STATUS_LABEL: Record<string, string> = {
  ACTIVE: '활성',
  INACTIVE: '비활성',
  BANNED: '정지',
};

const STATUS_COLOR: Record<string, string> = {
  ACTIVE: 'bg-green-50 text-green-700',
  INACTIVE: 'bg-gray-100 text-gray-500',
  BANNED: 'bg-red-100 text-red-700',
};

const CHANGEABLE_ROLES = ['USER', 'MANAGER', 'ADMIN'];

export default function AdminCustomersPage() {
  const [keyword, setKeyword] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [users, setUsers] = useState<ApiUser[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [changingRole, setChangingRole] = useState<number | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const loadUsers = async () => {
      setLoading(true);
      setError('');

      try {
        const res = await userService.getAdminUsers({
          keyword: searchKeyword || undefined,
          page: page - 1,
          size: PAGE_SIZE,
        });
        if (!mounted) return;
        setUsers(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages || 1);
      } catch (err) {
        if (!mounted) return;
        setUsers([]);
        setTotalElements(0);
        setTotalPages(1);
        setError(err instanceof Error ? err.message : '고객 목록을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadUsers();

    return () => {
      mounted = false;
    };
  }, [searchKeyword, page, reloadKey]);

  const handleSearch = () => {
    setSearchKeyword(keyword);
    setPage(1);
  };

  const handleRoleChange = async (userId: number, newRole: string) => {
    if (!confirm(`역할을 "${ROLE_LABEL[newRole]}"로 변경하시겠습니까?`)) return;
    setChangingRole(userId);
    userService
      .changeAdminUserRole(userId, newRole)
      .then((updated) => {
        setUsers((prev) => prev.map((u) => u.id === userId ? { ...u, role: updated.role } : u));
      })
      .catch((err) => alert(err instanceof Error ? err.message : '역할 변경 실패'))
      .finally(() => setChangingRole(null));
  };

  return (
    <AdminLayout title="고객 관리">
      <div className="flex items-center justify-between mb-5">
        <p className="text-sm text-[#8a9bb5]">
          총 <span className="font-semibold text-[#1a1f2e]">{totalElements}</span>명
        </p>
      </div>

      <div className="bg-white border border-[#e8eaf0] p-4 mb-4 flex gap-3">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="이름, 이메일 검색"
          className="flex-1 border border-[#e0e0e0] px-3 py-2 text-sm outline-none focus:border-[#1a1f2e]"
        />
        <Button variant="primary" size="sm" onClick={handleSearch}>검색</Button>
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
        <DataTable<ApiUser>
          keyField="id"
          data={users}
          emptyMessage="고객 데이터가 없습니다."
          columns={[
            { key: 'id', header: 'ID' },
            {
              key: 'name',
              header: '이름',
              render: (row) => (
                <div>
                  <p className="font-medium text-[#222]">{row.name}</p>
                  <p className="text-xs text-[#999]">{row.email}</p>
                </div>
              ),
            },
            {
              key: 'phone',
              header: '연락처',
              render: (row) => <span>{row.phone ?? '—'}</span>,
            },
            {
              key: 'role',
              header: '역할',
              render: (row) => (
                <div className="flex items-center gap-2">
                  <span className={`text-xs font-medium px-2 py-0.5 ${ROLE_COLOR[row.role] ?? ''}`}>
                    {ROLE_LABEL[row.role] ?? row.role}
                  </span>
                  {row.role !== 'SUPER_ADMIN' && (
                    <select
                      value={row.role}
                      disabled={changingRole === row.id}
                      onChange={(e) => handleRoleChange(row.id, e.target.value)}
                      className="text-xs border border-[#e0e0e0] px-1.5 py-0.5 outline-none bg-white text-[#555] disabled:opacity-50"
                    >
                      {CHANGEABLE_ROLES.map((r) => (
                        <option key={r} value={r}>{ROLE_LABEL[r]}</option>
                      ))}
                    </select>
                  )}
                </div>
              ),
            },
            {
              key: 'status',
              header: '상태',
              render: (row) => (
                <span className={`text-xs font-medium px-2 py-0.5 ${STATUS_COLOR[row.status] ?? ''}`}>
                  {STATUS_LABEL[row.status] ?? row.status}
                </span>
              ),
            },
            {
              key: 'createdAt',
              header: '가입일',
              render: (row) => formatDate(row.createdAt),
            },
            {
              key: 'orderCount',
              header: '주문 수',
              render: (row) => `${row.orderCount}건`,
            },
            {
              key: 'totalOrderAmount',
              header: '누적 주문금액',
              render: (row) => formatPrice(row.totalOrderAmount),
            },
          ]}
        />
      )}

      {!loading && !error && (
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      )}
    </AdminLayout>
  );
}
