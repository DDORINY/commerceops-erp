import type { User } from '@/features/auth/types';
import { storage } from './storage';

export function getAccessToken(): string | null {
  return storage.get('accessToken');
}

export function setAccessToken(token: string): void {
  storage.set('accessToken', token);
}

export function removeAccessToken(): void {
  storage.remove('accessToken');
}

export function getStoredUser(): User | null {
  const raw = storage.get('user');
  if (!raw) return null;
  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
}

export function setStoredUser(user: User): void {
  storage.set('user', JSON.stringify(user));
}

export function clearAuth(): void {
  storage.remove('accessToken');
  storage.remove('user');
}

export function isAdmin(): boolean {
  const user = getStoredUser();
  return user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN';
}

export function isAdminOrManager(): boolean {
  const user = getStoredUser();
  return user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN' || user?.role === 'MANAGER';
}

export function getUserRole(): string | null {
  return getStoredUser()?.role ?? null;
}
