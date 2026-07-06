'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import AdminLayout from '@/components/admin/AdminLayout';
import DataTable from '@/components/admin/DataTable';
import Pagination from '@/components/common/Pagination';
import Button from '@/components/common/Button';
import { auditService, type ApiAuditLog, type AuditLogFilters } from '@/lib/services/auditService';
import { formatDateTime } from '@/lib/format';

const PAGE_SIZE = 10;

const T = {
  title: '\uad00\ub9ac\uc790 \uc791\uc5c5 \uc774\ub825',
  total: '\ucd1d',
  count: '\uac74',
  description: '\uc8fc\uc694 \uad00\ub9ac\uc790 \ubcc0\uacbd \uc791\uc5c5\uacfc \uad8c\ud55c \uac70\ubd80 \uc774\ub825\uc744 \ud655\uc778\ud569\ub2c8\ub2e4.',
  settings: '\uc124\uc815\uc73c\ub85c',
  actorSearch: '\uc791\uc5c5\uc790 \uac80\uc0c9',
  allActions: '\uc804\uccb4 \uc791\uc5c5 \uc720\ud615',
  targetType: '\ub300\uc0c1 \ud0c0\uc785',
  targetId: '\ub300\uc0c1 ID',
  reset: '\ucd08\uae30\ud654',
  search: '\uac80\uc0c9',
  loading: '\uc791\uc5c5 \uc774\ub825\uc744 \ubd88\ub7ec\uc624\ub294 \uc911...',
  loadFailed: '\uad00\ub9ac\uc790 \uc791\uc5c5 \uc774\ub825\uc744 \ubd88\ub7ec\uc624\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4.',
  detailFailed: '\uc791\uc5c5 \uc774\ub825 \uc0c1\uc138\ub97c \ubd88\ub7ec\uc624\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4.',
  retry: '\ub2e4\uc2dc \ubd88\ub7ec\uc624\uae30',
  empty: '\uad00\ub9ac\uc790 \uc791\uc5c5 \uc774\ub825\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.',
  createdAt: '\uc791\uc5c5\uc77c\uc2dc',
  actor: '\uc791\uc5c5\uc790',
  actionType: '\uc791\uc5c5 \uc720\ud615',
  target: '\ub300\uc0c1',
  summary: '\ubcc0\uacbd \uc694\uc57d',
  detail: '\uc0c1\uc138',
  view: '\ubcf4\uae30',
  close: '\ub2eb\uae30',
  request: '\uc694\uccad',
  statusChange: '\uc0c1\ud0dc \ubcc0\uacbd',
  ip: 'IP',
};

const ACTION_TYPES = [
  'PRODUCT_CREATED',
  'PRODUCT_UPDATED',
  'PRODUCT_DELETED',
  'PRODUCT_STATUS_CHANGED',
  'PRODUCT_BULK_STATUS_CHANGED',
  'CATEGORY_CREATED',
  'CATEGORY_UPDATED',
  'BANNER_CREATED',
  'BANNER_UPDATED',
  'ORDER_STATUS_CHANGED',
  'PAYMENT_CANCELLED',
  'INVENTORY_ADJUSTED',
  'INVENTORY_INBOUNDED',
  'WAREHOUSE_CREATED',
  'STOCK_TRANSFERRED',
  'COUPON_CREATED',
  'COUPON_DELETED',
  'REVIEW_HIDDEN',
  'REVIEW_SHOWN',
  'REVIEW_DELETED',
  'INQUIRY_ANSWERED',
  'INQUIRY_CLOSED',
  'STAFF_CREATED',
  'STAFF_UPDATED',
  'STAFF_STATUS_CHANGED',
  'PERMISSION_GROUP_CREATED',
  'PERMISSION_GROUP_UPDATED',
  'USER_PERMISSION_GROUPS_UPDATED',
  'PERMISSION_MATRIX_UPDATED',
  'MENU_PERMISSION_UPDATED',
  'PERMISSION_DENIED',
];

function toDateTimeParam(value: string) {
  return value ? `${value}:00` : undefined;
}

function formatJson(value: string | null) {
  if (!value) return '-';
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
}

export default function AdminSettingsAuditLogsPage() {
  const [logs, setLogs] = useState<ApiAuditLog[]>([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [filters, setFilters] = useState({
    actorKeyword: '',
    actionType: '',
    targetType: '',
    targetId: '',
    dateFrom: '',
    dateTo: '',
  });
  const [appliedFilters, setAppliedFilters] = useState<AuditLogFilters>({});
  const [selectedLog, setSelectedLog] = useState<ApiAuditLog | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  useEffect(() => {
    let mounted = true;

    const loadLogs = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await auditService.getAuditLogs(appliedFilters, page - 1, PAGE_SIZE);
        if (!mounted) return;
        setLogs(res.content);
        setTotalPages(res.totalPages || 1);
        setTotalElements(res.totalElements);
      } catch (err) {
        if (!mounted) return;
        setLogs([]);
        setTotalPages(1);
        setTotalElements(0);
        setError(err instanceof Error ? err.message : T.loadFailed);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadLogs();

    return () => {
      mounted = false;
    };
  }, [page, reloadKey, appliedFilters]);

  const applyFilters = () => {
    setSelectedLog(null);
    setPage(1);
    setAppliedFilters({
      actorKeyword: filters.actorKeyword.trim() || undefined,
      actionType: filters.actionType || undefined,
      targetType: filters.targetType.trim().toUpperCase() || undefined,
      targetId: filters.targetId.trim() || undefined,
      dateFrom: toDateTimeParam(filters.dateFrom),
      dateTo: toDateTimeParam(filters.dateTo),
    });
  };

  const resetFilters = () => {
    setFilters({ actorKeyword: '', actionType: '', targetType: '', targetId: '', dateFrom: '', dateTo: '' });
    setSelectedLog(null);
    setPage(1);
    setAppliedFilters({});
  };

  const loadDetail = async (logId: number) => {
    setDetailLoading(true);
    try {
      const detail = await auditService.getAuditLog(logId);
      setSelectedLog(detail);
    } catch (err) {
      setError(err instanceof Error ? err.message : T.detailFailed);
    } finally {
      setDetailLoading(false);
    }
  };

  return (
    <AdminLayout title={T.title}>
      <div className="space-y-5">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-sm text-[#8a9bb5]">
              {T.total} <span className="font-semibold text-[#1a1f2e]">{totalElements}</span>{T.count}
            </p>
            <p className="mt-1 text-xs text-[#9aa6b8]">{T.description}</p>
          </div>
          <Link href="/admin/settings">
            <Button variant="outline" size="sm">{T.settings}</Button>
          </Link>
        </div>

        <div className="border border-[#e8eaf0] bg-white p-4">
          <div className="grid gap-3 md:grid-cols-3 xl:grid-cols-6">
            <input
              value={filters.actorKeyword}
              onChange={(event) => setFilters((prev) => ({ ...prev, actorKeyword: event.target.value }))}
              placeholder={T.actorSearch}
              className="border border-[#d9dde7] px-3 py-2 text-sm"
            />
            <select
              value={filters.actionType}
              onChange={(event) => setFilters((prev) => ({ ...prev, actionType: event.target.value }))}
              className="border border-[#d9dde7] px-3 py-2 text-sm"
            >
              <option value="">{T.allActions}</option>
              {ACTION_TYPES.map((actionType) => (
                <option key={actionType} value={actionType}>{actionType}</option>
              ))}
            </select>
            <input
              value={filters.targetType}
              onChange={(event) => setFilters((prev) => ({ ...prev, targetType: event.target.value }))}
              placeholder={T.targetType}
              className="border border-[#d9dde7] px-3 py-2 text-sm"
            />
            <input
              value={filters.targetId}
              onChange={(event) => setFilters((prev) => ({ ...prev, targetId: event.target.value.replace(/[^0-9]/g, '') }))}
              placeholder={T.targetId}
              className="border border-[#d9dde7] px-3 py-2 text-sm"
            />
            <input
              type="datetime-local"
              value={filters.dateFrom}
              onChange={(event) => setFilters((prev) => ({ ...prev, dateFrom: event.target.value }))}
              className="border border-[#d9dde7] px-3 py-2 text-sm"
            />
            <input
              type="datetime-local"
              value={filters.dateTo}
              onChange={(event) => setFilters((prev) => ({ ...prev, dateTo: event.target.value }))}
              className="border border-[#d9dde7] px-3 py-2 text-sm"
            />
          </div>
          <div className="mt-3 flex justify-end gap-2">
            <Button variant="outline" size="sm" onClick={resetFilters}>{T.reset}</Button>
            <Button size="sm" onClick={applyFilters}>{T.search}</Button>
          </div>
        </div>

        {loading ? (
          <div className="border border-[#e8eaf0] bg-white py-12 text-center text-sm text-[#8a9bb5]">
            {T.loading}
          </div>
        ) : error ? (
          <div className="border border-[#f1c7c7] bg-white px-5 py-12 text-center">
            <p className="text-sm text-[#c43a3a]">{error}</p>
            <Button variant="outline" size="sm" onClick={() => setReloadKey((prev) => prev + 1)} className="mt-4">
              {T.retry}
            </Button>
          </div>
        ) : (
          <DataTable<ApiAuditLog>
            keyField="id"
            data={logs}
            emptyMessage={T.empty}
            columns={[
              {
                key: 'createdAt',
                header: T.createdAt,
                render: (row) => formatDateTime(row.createdAt),
              },
              {
                key: 'actor',
                header: T.actor,
                render: (row) => (
                  <div>
                    <p className="font-medium text-[#222]">{row.actorName || '-'}</p>
                    <p className="text-xs text-[#8a9bb5]">{row.actorEmail || '-'}</p>
                  </div>
                ),
              },
              { key: 'actionType', header: T.actionType },
              {
                key: 'target',
                header: T.target,
                render: (row) => `${row.targetType} #${row.targetId ?? '-'}`,
              },
              {
                key: 'summary',
                header: T.summary,
                render: (row) => row.summary || `${row.beforeStatus ?? '-'} -> ${row.afterStatus ?? '-'}`,
              },
              {
                key: 'actions',
                header: T.detail,
                render: (row) => (
                  <Button variant="outline" size="sm" onClick={() => loadDetail(row.id)} disabled={detailLoading}>
                    {T.view}
                  </Button>
                ),
              },
            ]}
          />
        )}

        {!loading && !error && (
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        )}

        {selectedLog && (
          <section className="border border-[#d9dde7] bg-white p-5">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 className="text-base font-semibold text-[#1a1f2e]">{T.title} {T.detail}</h2>
                <p className="mt-1 text-xs text-[#8a9bb5]">#{selectedLog.id} / {formatDateTime(selectedLog.createdAt)}</p>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedLog(null)}>{T.close}</Button>
            </div>
            <div className="mt-4 grid gap-3 text-sm md:grid-cols-2">
              <p><span className="text-[#8a9bb5]">{T.actor}</span><br />{selectedLog.actorName} ({selectedLog.actorEmail})</p>
              <p><span className="text-[#8a9bb5]">{T.actionType}</span><br />{selectedLog.actionType}</p>
              <p><span className="text-[#8a9bb5]">{T.target}</span><br />{selectedLog.targetType} #{selectedLog.targetId ?? '-'}</p>
              <p><span className="text-[#8a9bb5]">{T.request}</span><br />{selectedLog.requestMethod || '-'} {selectedLog.requestPath || ''}</p>
              <p><span className="text-[#8a9bb5]">{T.ip}</span><br />{selectedLog.ipAddress || '-'}</p>
              <p><span className="text-[#8a9bb5]">{T.statusChange}</span><br />{`${selectedLog.beforeStatus ?? '-'} -> ${selectedLog.afterStatus ?? '-'}`}</p>
            </div>
            <div className="mt-4 grid gap-3 lg:grid-cols-3">
              <pre className="max-h-64 overflow-auto bg-[#f8f9fb] p-3 text-xs text-[#333]">{formatJson(selectedLog.beforeJson)}</pre>
              <pre className="max-h-64 overflow-auto bg-[#f8f9fb] p-3 text-xs text-[#333]">{formatJson(selectedLog.afterJson)}</pre>
              <pre className="max-h-64 overflow-auto bg-[#f8f9fb] p-3 text-xs text-[#333]">{formatJson(selectedLog.metadataJson)}</pre>
            </div>
          </section>
        )}
      </div>
    </AdminLayout>
  );
}
