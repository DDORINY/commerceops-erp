# 프론트엔드 구조 문서

기준 버전: `v0.2.4`
기준 코드: `frontend/src`

## 기술 스택

- Next.js App Router
- React 19
- TypeScript
- 서비스 레이어: `frontend/src/lib/services/*`
- 공통 API 클라이언트: `frontend/src/lib/api.ts`

## 라우트 구조

### 사용자 쇼핑 화면

| Route | 파일 | 실제 데이터 소스 |
| --- | --- | --- |
| `/` | `app/page.tsx` | `productService.getProducts` |
| `/products` | `app/products/page.tsx` | `productService.getProducts`, `getCategories` |
| `/products/[id]` | `app/products/[id]/page.tsx` | 상품 상세, 장바구니, 위시리스트, 문의, 리뷰 service |
| `/cart` | `app/cart/page.tsx` | `cartService` |
| `/orders` | `app/orders/page.tsx` | `orderService.getOrders` |
| `/orders/checkout` | `app/orders/checkout/page.tsx` | `cartService`, `orderService`, `paymentService`, `couponService` |
| `/orders/[id]` | `app/orders/[id]/page.tsx` | 주문 상세, 배송, 반품, 리뷰 service |
| `/mypage` | `app/mypage/page.tsx` | `authService.me`, 주문, 문의, 반품 service |
| `/wishlist` | `app/wishlist/page.tsx` | `wishlistService` |
| `/login` | `app/login/page.tsx` | `authService.login` |
| `/signup` | `app/signup/page.tsx` | `authService.signup` |

### 관리자 화면

| Route | 파일 | 실제 데이터 소스 |
| --- | --- | --- |
| `/admin` | `app/admin/page.tsx` | `adminService`, `orderService` |
| `/admin/customers` | `app/admin/customers/page.tsx` | `userService.getAdminUsers` |
| `/admin/orders` | `app/admin/orders/page.tsx` | `orderService.getAdminOrders`, `updateOrderStatus` |
| `/admin/products` | `app/admin/products/page.tsx` | `productService.getAdminProducts` |
| `/admin/products/new` | `app/admin/products/new/page.tsx` | `productService.createProduct`, `getCategories`, `mediaService.uploadProductImage` |
| `/admin/products/[id]` | `app/admin/products/[id]/page.tsx` | `productService.getAdminProduct`, update/delete, `mediaService.uploadProductImage` |
| `/admin/inquiries` | `app/admin/inquiries/page.tsx` | `inquiryService.getAdminInquiries`, answer/close |
| `/admin/reviews` | `app/admin/reviews/page.tsx` | `reviewService.getAdminReviews`, hide/show/delete, `auditService.getAuditLogs` |
| `/admin/accounting` | `app/admin/accounting/page.tsx` | `accountingService` |
| `/admin/sales` | `app/admin/sales/page.tsx` | `adminService` dashboard sales/top products |
| `/admin/inventory` | `app/admin/inventory/page.tsx` | `adminService.getInventory`, inbound/adjust |
| `/admin/shipments` | `app/admin/shipments/page.tsx` | `shipmentService` |
| `/admin/returns` | `app/admin/returns/page.tsx` | `returnService` |
| `/admin/coupons` | `app/admin/coupons/page.tsx` | `couponService` |
| `/admin/warehouses` | `app/admin/warehouses/page.tsx` | `warehouseService` |

## 서비스 레이어

| 파일 | 역할 |
| --- | --- |
| `api.ts` | API base URL, JSON fetch, Bearer 토큰 자동 추가, 401 refresh 재시도와 세션 만료 처리 |
| `authService.ts` | 로그인, 회원가입, 내 정보, 토큰 갱신, 로그아웃 |
| `productService.ts` | 사용자/관리자 상품, 카테고리, 상품 CRUD |
| `mediaService.ts` | 관리자 상품 이미지 multipart 업로드 |
| `cartService.ts` | 장바구니 조회/추가/수량변경/삭제 |
| `orderService.ts` | 주문 생성/조회/상세/취소, 관리자 주문 목록/상태 변경 |
| `paymentService.ts` | 결제 승인, 결제 취소, 하위 호환 모의 결제 완료 API 호출 |
| `wishlistService.ts` | 찜 토글/목록/상태 |
| `inquiryService.ts` | 사용자/상품 문의, 관리자 문의 답변/종료 |
| `reviewService.ts` | 사용자 리뷰, 상품 리뷰, 관리자 리뷰 목록/숨김/해제/삭제 |
| `auditService.ts` | 관리자 작업 이력 조회 |
| `returnService.ts` | 반품 요청/목록, 관리자 승인/거절 |
| `shipmentService.ts` | 사용자 배송 조회, 관리자 배송 처리 |
| `couponService.ts` | 쿠폰 검증, 관리자 쿠폰 CRUD 일부 |
| `adminService.ts` | 관리자 대시보드, 매출, 재고 요약 |
| `accountingService.ts` | 관리자 회계 요약/내역 |
| `warehouseService.ts` | 창고, 창고별 재고, 이동, 할당 |
| `userService.ts` | 관리자 고객 목록/권한 변경 |

## 타입 구조

- API 응답 타입은 각 service 파일에 `Api*` 인터페이스로 정의한다.
- UI 공용/도메인 보조 타입은 `frontend/src/features/*/types.ts`에 남아 있다.
- v0.1.6에서 `frontend/src/features/*/mock.ts` 미사용 mock 파일은 제거했다.

## 인증 흐름

- 로그인 성공 시 `accessToken`, `refreshToken`을 `localStorage`에 저장한다.
- `apiClient`는 브라우저 환경에서 `localStorage.accessToken`을 읽어 `Authorization: Bearer {token}` 헤더를 추가한다.
- 401 응답 시 `refreshToken`으로 1회 재발급을 시도하고, 실패하면 `accessToken`, `refreshToken`, `user`를 제거하고 `/login`으로 이동한다.
- 로그아웃은 서버 no-op API 호출 후 클라이언트 토큰과 사용자 정보를 제거한다.
- v0.2.1 최소 구현에서는 refresh token을 `localStorage`에 저장한다. HttpOnly cookie, 서버 저장소 기반 rotation, 탈취 감지는 후속 보안 고도화 범위다.

## 현재 UI 상태 처리 기준

- v0.1.1~v0.1.6 작업으로 주요 관리자/사용자 화면에 로딩, 에러, 빈 상태가 최소 수준으로 추가됐다.
- 일부 화면의 상세 UX, 폼 검증, 토스트/모달 표준화는 v0.1.8 또는 v0.2 이후 보완 대상이다.

## 미구현/예정으로 남은 프론트 기능

- 실제 PG 결제 UI/승인 콜백. 현재 checkout은 `/api/payments/approve`를 호출하되 provider는 백엔드 `MOCK_PROVIDER` 기반이다.
- S3/CDN, 이미지 리사이징, 썸네일, 다중 이미지 갤러리.
- 전체 관리자 기능 감사 로그 UI. 현재는 관리자 리뷰 화면의 최근 리뷰 감사 로그만 표시한다.
- 고급 차트/BI 라이브러리 기반 분석 화면.
- 피킹/패킹/출고 자동화 화면.
