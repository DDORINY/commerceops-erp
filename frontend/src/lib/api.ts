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
  const token =
    typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });
  } catch {
    throw new ApiError(0, '서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
  }

  if (response.status === 401) {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      sessionStorage.setItem('authMessage', '로그인이 필요하거나 세션이 만료되었습니다. 다시 로그인해주세요.');
      const next = `${window.location.pathname}${window.location.search}`;
      window.location.href = `/login?next=${encodeURIComponent(next)}`;
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
