export type UserRole = 'USER' | 'MANAGER' | 'ADMIN' | 'SUPER_ADMIN';

export interface User {
  id: number;
  email: string;
  name: string;
  phone: string;
  role: UserRole;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  user: User;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phone: string;
}
