# 인증 토큰 저장 구조 점검 및 마이그레이션 계획

## 현재 흐름

로그인 페이지가 `/auth/login` 응답의 access token과 refresh token을 각각 `setAccessToken`/`setRefreshToken`으로 `localStorage`에 저장한다. `api.ts`의 `requestWithAuth`가 access token을 `Authorization: Bearer` 헤더에 주입하고, 401 응답 시 `localStorage`의 refresh token을 `/auth/refresh`로 전송해 두 토큰을 다시 저장한다. logout은 `/auth/logout` 호출 후 브라우저 저장소를 삭제하지만 서버 토큰 폐기는 아직 없다.

## 현재 사용 지점

- `frontend/src/lib/storage.ts`: localStorage 추상화
- `frontend/src/lib/auth.ts`: access/refresh token 저장·삭제
- `frontend/src/lib/api.ts`: 직접 localStorage 접근, Authorization 주입, refresh 재시도
- `frontend/src/lib/services/authService.ts`: 로그인·refresh·logout API
- `frontend/src/app/login/page.tsx`: 로그인 응답 저장
- `frontend/src/components/admin/AdminTopbar.tsx`, `frontend/src/components/shop/ShopHeader.tsx`: logout
- 상품 최근 조회 기록도 `localStorage`를 사용하지만 인증 토큰과 분리된 비민감 데이터다.

## HttpOnly refresh cookie 전환 영향 범위

백엔드 `AuthController`, `AuthService`, 로그인/refresh 응답 DTO를 수정해 refresh token을 `HttpOnly; Secure; SameSite=Lax` 쿠키로 발급하고 JSON 응답에서는 제거한다. `api.ts`의 refresh 요청은 `credentials: 'include'`를 사용하며 refresh token을 읽거나 JSON body로 전송하지 않는다. CORS는 명시적 HTTPS origin과 credentials를 유지해야 한다.

## CSRF 방어

쿠키 인증을 쓰는 refresh/logout 요청에는 CSRF 토큰 또는 SameSite 정책이 필요하다. 서버 발급 CSRF 토큰을 별도 non-HttpOnly 쿠키와 헤더로 비교하거나, refresh/logout 경로에 Origin 검사를 추가한다. 쿠키 전환 시 Spring Security CSRF를 전역 비활성화하지 않는다.

## access token 메모리 저장 영향

`storage.ts`의 access token 저장 API를 메모리 모듈로 바꾸고, 새로고침 시 refresh cookie로 access token을 재발급한다. `api.ts`의 SSR 경로와 브라우저 재시도 로직이 영향을 받으며, 탭 간 공유가 사라지므로 로그인 상태 초기화 요청을 추가해야 한다.

## 단계별 순서

1. 서버 refresh token rotation·폐기 저장소와 logout 무효화를 먼저 추가한다.
2. refresh token을 HttpOnly cookie로 발급하고 CSRF/Origin 검사를 적용한다.
3. 프런트에서 refresh token의 모든 localStorage 읽기·쓰기를 제거한다.
4. access token을 메모리에만 유지하고 최초 로드 시 cookie refresh를 수행한다.
5. 기존 localStorage 토큰을 즉시 삭제하고 만료 기간 후 legacy 응답 필드를 제거한다.
