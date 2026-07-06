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

export interface Permission {
  id: number;
  code: string;
  name: string;
  domain: string;
  action: string;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EffectivePermission {
  userId: number;
  userRole: string;
  permissionCodes: string[];
}

export interface AdminMenuPermission {
  id: number;
  menuKey: string;
  menuLabel: string;
  menuPath: string;
  requiredPermissionCode: string;
  visible: boolean;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface AdminMenuPermissionUpdateItem {
  menuKey: string;
  menuLabel: string;
  menuPath: string;
  requiredPermissionCode: string;
  visible: boolean;
  sortOrder: number;
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

  getPermissions: () => apiClient<Permission[]>('/admin/permissions'),

  getGroupPermissions: (groupId: number) =>
    apiClient<Permission[]>(`/admin/permission-groups/${groupId}/permissions`),

  updateGroupPermissions: (groupId: number, permissionIds: number[]) =>
    apiClient<Permission[]>(`/admin/permission-groups/${groupId}/permissions`, {
      method: 'PUT',
      body: JSON.stringify({ permissionIds }),
    }),

  getUserEffectivePermissions: (userId: number) =>
    apiClient<EffectivePermission>(`/admin/users/${userId}/permissions`),

  getMyEffectivePermissions: () =>
    apiClient<EffectivePermission>('/admin/users/me/permissions'),

  getMenuPermissions: () => apiClient<AdminMenuPermission[]>('/admin/menu-permissions'),

  updateMenuPermissions: (items: AdminMenuPermissionUpdateItem[]) =>
    apiClient<AdminMenuPermission[]>('/admin/menu-permissions', {
      method: 'PUT',
      body: JSON.stringify({ items }),
    }),
};
