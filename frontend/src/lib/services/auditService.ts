import { apiClient, type PageResponse } from '@/lib/api';

export interface ApiAuditLog {
  id: number;
  actorId: number | null;
  actorEmail: string;
  actorName: string;
  actionType: string;
  targetType: string;
  targetId: number | null;
  beforeStatus: string | null;
  afterStatus: string | null;
  summary: string | null;
  ipAddress: string | null;
  userAgent: string | null;
  requestMethod: string | null;
  requestPath: string | null;
  beforeJson: string | null;
  afterJson: string | null;
  metadataJson: string | null;
  createdAt: string;
}

export interface AuditLogFilters {
  actorKeyword?: string;
  actionType?: string;
  targetType?: string;
  targetId?: string;
  dateFrom?: string;
  dateTo?: string;
}

export const auditService = {
  getAuditLogs: (filters: AuditLogFilters = {}, page = 0, size = 10) => {
    const qs = new URLSearchParams();
    if (filters.actorKeyword) qs.set('actorKeyword', filters.actorKeyword);
    if (filters.actionType) qs.set('actionType', filters.actionType);
    if (filters.targetType) qs.set('targetType', filters.targetType);
    if (filters.targetId) qs.set('targetId', filters.targetId);
    if (filters.dateFrom) qs.set('dateFrom', filters.dateFrom);
    if (filters.dateTo) qs.set('dateTo', filters.dateTo);
    qs.set('page', String(page));
    qs.set('size', String(size));
    return apiClient<PageResponse<ApiAuditLog>>(`/admin/audit-logs?${qs.toString()}`);
  },
  getAuditLog: (auditLogId: number) => apiClient<ApiAuditLog>(`/admin/audit-logs/${auditLogId}`),
};
