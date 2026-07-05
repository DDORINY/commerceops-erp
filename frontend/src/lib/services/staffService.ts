import { apiClient, type PageResponse } from '@/lib/api';

export type UserRole = 'MANAGER' | 'ADMIN' | 'SUPER_ADMIN';
export type EmploymentStatus = 'ACTIVE' | 'ON_LEAVE' | 'RESIGNED';

export interface DepartmentOption {
  id: number;
  name: string;
  code: string | null;
  parentId: number | null;
  sortOrder: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PositionOption {
  id: number;
  name: string;
  level: number;
  sortOrder: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface StaffProfile {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  userRole: UserRole;
  userStatus: string;
  departmentId: number | null;
  departmentName: string | null;
  positionId: number | null;
  positionName: string | null;
  employeeNo: string | null;
  employmentStatus: EmploymentStatus;
  joinedAt: string | null;
  leftAt: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface StaffListParams {
  keyword?: string;
  departmentId?: number;
  positionId?: number;
  employmentStatus?: EmploymentStatus;
  active?: boolean;
  role?: UserRole;
  page?: number;
  size?: number;
}

export interface StaffCreateRequest {
  email: string;
  password: string;
  name: string;
  role: UserRole;
  employeeNo?: string;
  departmentId?: number;
  positionId?: number;
  employmentStatus?: EmploymentStatus;
  joinedAt?: string;
  active?: boolean;
}

export interface StaffUpdateRequest {
  name?: string;
  role?: UserRole;
  employeeNo?: string;
  departmentId?: number;
  positionId?: number;
  employmentStatus?: EmploymentStatus;
  joinedAt?: string;
  leftAt?: string;
  active?: boolean;
}

function toQuery(params: StaffListParams) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.set(key, String(value));
    }
  });
  const qs = query.toString();
  return qs ? `?${qs}` : '';
}

export const staffService = {
  getDepartments: () => apiClient<DepartmentOption[]>('/admin/hr/departments'),

  getPositions: () => apiClient<PositionOption[]>('/admin/hr/positions'),

  getStaff: (params: StaffListParams = {}) =>
    apiClient<PageResponse<StaffProfile>>(`/admin/staff${toQuery(params)}`),

  getStaffDetail: (id: number) => apiClient<StaffProfile>(`/admin/staff/${id}`),

  createStaff: (request: StaffCreateRequest) =>
    apiClient<StaffProfile>('/admin/staff', {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  updateStaff: (id: number, request: StaffUpdateRequest) =>
    apiClient<StaffProfile>(`/admin/staff/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(request),
    }),

  updateEmploymentStatus: (id: number, employmentStatus: EmploymentStatus, leftAt?: string) =>
    apiClient<StaffProfile>(`/admin/staff/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ employmentStatus, leftAt }),
    }),

  updateActive: (id: number, active: boolean) =>
    apiClient<StaffProfile>(`/admin/staff/${id}/active`, {
      method: 'PATCH',
      body: JSON.stringify({ active }),
    }),
};
