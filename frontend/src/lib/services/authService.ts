import { apiClient } from '@/lib/api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginUser {
  id: number;
  email: string;
  name: string;
  role: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: LoginUser;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phone?: string;
}

export interface SignupResponse {
  id: number;
  email: string;
  name: string;
}

export interface MeResponse {
  id: number;
  email: string;
  name: string;
  phone: string | null;
  role: string;
  status: string;
}

export const authService = {
  login: (data: LoginRequest) =>
    apiClient<LoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  refresh: (refreshToken: string) =>
    apiClient<RefreshTokenResponse>('/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    }),
  logout: () =>
    apiClient<void>('/auth/logout', {
      method: 'POST',
    }),
  signup: (data: SignupRequest) =>
    apiClient<SignupResponse>('/auth/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  me: () => apiClient<MeResponse>('/auth/me'),
};
