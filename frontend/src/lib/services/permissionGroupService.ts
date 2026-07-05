import { apiClient } from '@/lib/api';

export interface PermissionGroup {
  id: number;
  name: string;
  code: string;
  description: string | null;
  systemGroup: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PermissionGroupCreateRequest {
  name: string;
  code: string;
  description?: string;
  active?: boolean;
}

export interface PermissionGroupUpdateRequest {
  name: string;
  description?: string;
}

export interface UserPermissionGroupAssignment {
  assignmentId: number;
  userId: number;
  permissionGroupId: number;
  permissionGroupName: string;
  permissionGroupCode: string;
  systemGroup: boolean;
  active: boolean;
  createdBy: number | null;
  createdAt: string;
}

export const permissionGroupService = {
  getPermissionGroups: () =>
    apiClient<PermissionGroup[]>('/admin/permission-groups'),

  getPermissionGroup: (groupId: number) =>
    apiClient<PermissionGroup>(`/admin/permission-groups/${groupId}`),

  createPermissionGroup: (data: PermissionGroupCreateRequest) =>
    apiClient<PermissionGroup>('/admin/permission-groups', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updatePermissionGroup: (groupId: number, data: PermissionGroupUpdateRequest) =>
    apiClient<PermissionGroup>(`/admin/permission-groups/${groupId}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  updatePermissionGroupActive: (groupId: number, active: boolean) =>
    apiClient<PermissionGroup>(`/admin/permission-groups/${groupId}/active`, {
      method: 'PATCH',
      body: JSON.stringify({ active }),
    }),

  getUserPermissionGroups: (userId: number) =>
    apiClient<UserPermissionGroupAssignment[]>(`/admin/users/${userId}/permission-groups`),

  updateUserPermissionGroups: (userId: number, permissionGroupIds: number[]) =>
    apiClient<UserPermissionGroupAssignment[]>(`/admin/users/${userId}/permission-groups`, {
      method: 'PUT',
      body: JSON.stringify({ permissionGroupIds }),
    }),
};
