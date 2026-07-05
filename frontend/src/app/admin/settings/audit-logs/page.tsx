'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { auditService, type ApiAuditLog } from '@/lib/services/auditService';
import { formatDateTime } from '@/lib/format';

const PAGE_SIZE = 10;

export default function AdminSettingsAuditLogsPage() {
  const [logs, setLogs] = useState<ApiAuditLog[]>([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    const loadLogs = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await auditService.getAuditLogs(undefined, page - 1, PAGE_SIZE);
        if (!mounted) return;
        setLogs(res.content);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements);
      } catch (err) {
        if (!mounted) return;
        setLogs([]);
        setTotalPages(1);
        setTotalElements(0);
        setError(err instanceof Error ? err.message : '관리자 작업 이력을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadLogs();

    return () => {
      mounted = false;
    };
  }, [page, reloadKey]);

  return (
    <AdminLayout title="관리자 작업 이력">
      <div className="space-y-5">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-[#8a9bb5]">
              총 <span className="font-semibold text-[#1a1f2e]">{totalElements}</span>건
            </p>
            <p className="mt-1 text-xs text-[#9aa6b8]">
              v0.2.4에서 추가된 audit_logs API를 재사용합니다.
            </p>
          </div>
          <Link href="/admin/settings">
            <Button variant="outline" size="sm">설정으로</Button>
          </Link>
        </div>

        {loading ? (
          <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#8a9bb5]">
            작업 이력을 불러오는 중...
          </div>
        ) : error ? (
          <div className="border border-[#f1c7c7] bg-white px-5 py-12 text-center">
            <p className="text-sm text-[#c43a3a]">{error}</p>
            <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
              다시 불러오기
            </Button>
          </div>
        ) : (
          <DataTable<ApiAuditLog>
            keyField="id"
            data={logs}
            emptyMessage="관리자 작업 이력이 없습니다."
            columns={[
              {
                key: 'createdAt',
                header: '작업일시',
                render: (row) => formatDateTime(row.createdAt),
              },
              {
                key: 'actor',
                header: '작업자',
                render: (row) => (
                  <div>
                    <p className="font-medium text-[#222]">{row.actorName || '-'}</p>
                    <p className="text-xs text-[#8a9bb5]">{row.actorEmail || '-'}</p>
                  </div>
                ),
              },
              {
                key: 'actionType',
                header: '작업 유형',
              },
              {
                key: 'target',
                header: '대상',
                render: (row) => `${row.targetType} #${row.targetId}`,
              },
              {
                key: 'summary',
                header: '변경 요약',
                render: (row) => row.summary || `${row.beforeStatus ?? '-'} → ${row.afterStatus ?? '-'}`,
              },
            ]}
          />
        )}

        {!loading && !error && (
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        )}
      </div>
    </AdminLayout>
  );
}
