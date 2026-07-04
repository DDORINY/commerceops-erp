import { apiClient, type PageResponse } from '@/lib/api';

export interface ApiAuditLog {
  id: number;
  actorId: number | null;
  actorEmail: string;
  actorName: string;
  actionType: string;
  targetType: string;
  targetId: number;
  beforeStatus: string | null;
  afterStatus: string | null;
  summary: string | null;
  createdAt: string;
}

export const auditService = {
  getAuditLogs: (targetType?: string, page = 0, size = 10) => {
    const qs = new URLSearchParams();
    if (targetType) qs.set('targetType', targetType);
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiAuditLog>>(`/admin/audit-logs?${qs.toString()}`);
  },
};
