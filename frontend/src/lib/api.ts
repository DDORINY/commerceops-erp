export interface ApiResponse<T> {
  success: boolean;
  statusCode: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

let refreshPromise: Promise<string | null> | null = null;

export class ApiError extends Error {
  constructor(
    public statusCode: number,
    message: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export async function apiClient<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const response = await requestWithAuth(path, options);

  if (response.status === 401 && shouldAttemptRefresh(path)) {
    const refreshedToken = await refreshAccessToken();
    if (refreshedToken) {
      const retryResponse = await requestWithAuth(path, options, refreshedToken);
      return handleResponse<T>(retryResponse);
    }
  }

  return handleResponse<T>(response);
}

export async function publicApiClient<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const response = await requestWithoutAuth(path, options);
  return handleResponse<T>(response, { redirectOnUnauthorized: false });
}

export async function apiFormClient<T>(
  path: string,
  formData: FormData,
  options: RequestInit = {}
): Promise<T> {
  const response = await requestWithAuth(path, {
    ...options,
    method: options.method ?? 'POST',
    body: formData,
  });

  if (response.status === 401 && shouldAttemptRefresh(path)) {
    const refreshedToken = await refreshAccessToken();
    if (refreshedToken) {
      const retryResponse = await requestWithAuth(path, {
        ...options,
        method: options.method ?? 'POST',
        body: formData,
      }, refreshedToken);
      return handleResponse<T>(retryResponse);
    }
  }

  return handleResponse<T>(response);
}

async function requestWithAuth(
  path: string,
  options: RequestInit = {},
  overrideToken?: string
): Promise<Response> {
  const token =
    overrideToken ??
    (typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null);

  try {
    return await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });
  } catch {
    throw new ApiError(0, '서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
  }
}

async function requestWithoutAuth(
  path: string,
  options: RequestInit = {}
): Promise<Response> {
  try {
    return await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
        ...options.headers,
      },
    });
  } catch {
    throw new ApiError(0, '서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
  }
}

async function handleResponse<T>(
  response: Response,
  options: { redirectOnUnauthorized?: boolean } = {}
): Promise<T> {
  if (response.status === 401) {
    if (options.redirectOnUnauthorized !== false) {
      expireSession();
    }
    throw new ApiError(401, '로그인이 필요합니다.');
  }

  if (response.status === 403) {
    throw new ApiError(403, '접근 권한이 없습니다. 관리자 권한 또는 계정 상태를 확인해주세요.');
  }

  if (!response.ok) {
    let message = 'API 요청에 실패했습니다.';
    try {
      const errorBody = (await response.json()) as ApiResponse<null>;
      if (errorBody.message) message = errorBody.message;
    } catch {
      // ignore JSON parse errors
    }
    throw new ApiError(response.status, message);
  }

  const body = (await response.json()) as ApiResponse<T>;
  return body.data;
}

function shouldAttemptRefresh(path: string): boolean {
  if (typeof window === 'undefined') return false;
  if (path === '/auth/login' || path === '/auth/signup' || path === '/auth/refresh') return false;
  return Boolean(localStorage.getItem('refreshToken'));
}

async function refreshAccessToken(): Promise<string | null> {
  if (typeof window === 'undefined') return null;
  if (!refreshPromise) {
    refreshPromise = requestTokenRefresh().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

async function requestTokenRefresh(): Promise<string | null> {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) return null;

  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) return null;

    const body = (await response.json()) as ApiResponse<{
      accessToken: string;
      refreshToken: string;
    }>;
    localStorage.setItem('accessToken', body.data.accessToken);
    localStorage.setItem('refreshToken', body.data.refreshToken);
    return body.data.accessToken;
  } catch {
    return null;
  }
}

function expireSession(): void {
  if (typeof window === 'undefined') return;

  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  sessionStorage.setItem('authMessage', '로그인이 필요하거나 세션이 만료되었습니다. 다시 로그인해주세요.');

  if (!window.location.pathname.startsWith('/login')) {
    const next = `${window.location.pathname}${window.location.search}`;
    window.location.href = `/login?next=${encodeURIComponent(next)}`;
  }
}
