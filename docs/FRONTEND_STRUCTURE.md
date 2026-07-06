# 프론트엔드 구조 문서

기준 버전: `v0.3.7`
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
| `/` | `app/page.tsx` | `bannerService.getBanners`, `categoryService.getNavigationCategories`, `productService.getProducts` |
| `/products` | `app/products/page.tsx` | `productService.getProducts`, `getCategories`, URL category/keyword query |
| `/products/[id]` | `app/products/[id]/page.tsx` | 상품 상세, 상품 마스터 공개 필드, 상세 블록 렌더러, 구매 가능 여부, 장바구니, 위시리스트, 문의, 리뷰 service |
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
| `/admin/products` | `app/admin/products/page.tsx` | `productService.getAdminProducts`, 상품코드/브랜드/매입가/마진율 표시, 카테고리/재고/판매기간 필터, 선택 대량 상태 변경 |
| `/admin/categories` | `app/admin/categories/page.tsx` | `categoryService.getAdminCategoryTree`, create/update |
| `/admin/banners` | `app/admin/banners/page.tsx` | `bannerService.getAdminBanners`, create/update/deactivate |
| `/admin/products/new` | `app/admin/products/new/page.tsx` | `productService.createProduct`, getCategories, 이미지 업로드, 상품 마스터 입력 |
| `/admin/products/[id]` | `app/admin/products/[id]/page.tsx` | `productService.getAdminProduct`, update/delete, 이미지 업로드, 상품 마스터 수정, 상세 블록 편집, 운영 메모, 상태 변경 이력 |
| `/admin/inquiries` | `app/admin/inquiries/page.tsx` | `inquiryService.getAdminInquiries`, answer/close |
| `/admin/reviews` | `app/admin/reviews/page.tsx` | `reviewService.getAdminReviews`, hide/show/delete, `auditService.getAuditLogs` |
| `/admin/accounting` | `app/admin/accounting/page.tsx` | `accountingService` |
| `/admin/sales` | `app/admin/sales/page.tsx` | `adminService` dashboard sales/top products |
| `/admin/inventory` | `app/admin/inventory/page.tsx` | `adminService.getInventory`, inbound/adjust |
| `/admin/shipments` | `app/admin/shipments/page.tsx` | `shipmentService` |
| `/admin/returns` | `app/admin/returns/page.tsx` | `returnService` |
| `/admin/coupons` | `app/admin/coupons/page.tsx` | `couponService` |
| `/admin/warehouses` | `app/admin/warehouses/page.tsx` | `warehouseService` |
| `/admin/settings` | `app/admin/settings/page.tsx` | 시스템 설정 진입점 |
| `/admin/settings/audit-logs` | `app/admin/settings/audit-logs/page.tsx` | `auditService.getAuditLogs`, `auditService.getAuditLog`, 필터/상세 보기 |
| `/admin/settings/staff` | `app/admin/settings/staff/page.tsx` | `staffService`, 직원 목록/등록/수정/상태 변경 |
| `/admin/settings/permission-groups` | `app/admin/settings/permission-groups/page.tsx` | `permissionGroupService`, 권한 그룹 CRUD, 직원별 권한 그룹 할당 |
| `/admin/settings/roles` | `app/admin/settings/roles/page.tsx` | 기존 role과 permission group 병행 운영 안내 |
| `/admin/settings/menu-permissions` | `app/admin/settings/menu-permissions/page.tsx` | 권한 그룹별 기능 권한 매트릭스, 메뉴별 필요 권한 관리 |

## 서비스 레이어

| 파일 | 역할 |
| --- | --- |
| `api.ts` | API base URL, JSON fetch, Bearer 토큰 자동 추가, 401 refresh 재시도와 세션 만료 처리 |
| `authService.ts` | 로그인, 회원가입, 내 정보, 토큰 갱신, 로그아웃 |
| `productService.ts` | 사용자/관리자 상품, 카테고리, 상품 CRUD, 상품 마스터 타입, 상세 블록 타입/API, 대량 상태 변경, 상품 운영 메모, 상태 변경 이력. 관리자 타입은 매입가/마진율을 포함하고 사용자 타입은 내부 운영 필드를 제외 |
| `categoryService.ts` | 사용자 네비 카테고리, 관리자 카테고리 트리, 생성/수정 |
| `bannerService.ts` | 공개 메인 배너 조회, 관리자 배너 목록/상세/등록/수정/비활성화 |
| `mediaService.ts` | 관리자 상품 이미지 multipart 업로드 |
| `cartService.ts` | 장바구니 조회/추가/수량변경/삭제 |
| `orderService.ts` | 주문 생성/조회/상세/취소, 관리자 주문 목록/상태 변경 |
| `paymentService.ts` | 결제 승인, 결제 취소, 하위 호환 모의 결제 완료 API 호출 |
| `wishlistService.ts` | 찜 토글/목록/상태 |
| `inquiryService.ts` | 사용자/상품 문의, 관리자 문의 답변/종료 |
| `reviewService.ts` | 사용자 리뷰, 상품 리뷰, 관리자 리뷰 목록/숨김/해제/삭제 |
| `auditService.ts` | 관리자 작업 이력 목록/상세 조회, actor/action/target/date 필터 |
| `notificationService.ts` | 사용자 알림 목록/읽음 처리, 관리자 최근 알림 조회 |
| `opsAnalyticsService.ts` | 운영 분석 기초 overview 조회 |
| `returnService.ts` | 반품 요청/목록, 관리자 승인/거절 |
| `shipmentService.ts` | 사용자 배송 조회, 관리자 배송 처리 |
| `couponService.ts` | 쿠폰 검증, 관리자 쿠폰 CRUD 일부 |
| `adminService.ts` | 관리자 대시보드, 매출, 재고 요약 |
| `accountingService.ts` | 관리자 회계 요약/내역 |
| `warehouseService.ts` | 창고, 창고별 재고, 이동, 할당 |
| `userService.ts` | 관리자 고객 목록/권한 변경 |
| `staffService.ts` | 관리자 직원 목록/상세/등록/수정, 부서/직급 조회, 직원 상태 변경 |
| `permissionGroupService.ts` | 권한 그룹 목록/상세/생성/수정/활성 변경, 사용자 권한 그룹 조회/할당, 권한 목록/그룹별 권한/유효 권한/메뉴 권한 |

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
- 고급 차트/BI 라이브러리 기반 분석 화면. v0.2.8에서는 `opsAnalyticsService` 타입과 API 호출 기준만 추가했다.
- 피킹/패킹/출고 자동화 화면.

## v0.3.5 Product Sales Status UI

- `frontend/src/app/admin/products/page.tsx`: 판매 상태/전시 상태 필터와 상태 배지 표시.
- `frontend/src/app/admin/products/new/page.tsx`: 상품 등록 시 판매 상태, 전시 상태, 안전 재고 입력.
- `frontend/src/app/admin/products/[id]/page.tsx`: 상품 수정 시 판매 상태, 전시 상태, 안전 재고 입력과 상세 블록 편집 유지.
- `frontend/src/components/shop/ProductCard.tsx`, `frontend/src/app/products/[id]/page.tsx`: `purchasable`, `stockDisplayText` 기준으로 재고 배지와 구매 버튼 비활성화 처리.

## v0.3.5.1 Admin Navigation and Settings

- `frontend/src/components/admin/AdminSidebarV2.tsx`: 관리자 메뉴를 업무 영역별 아코디언 그룹으로 재구성했다.
- `frontend/src/components/admin/AdminSidebar.tsx`: 구형 사이드바 진입점은 `AdminSidebarV2`를 재사용한다.
- 권한별 메뉴 노출은 `getUserRole()` 기준으로 `SUPER_ADMIN`, `ADMIN`, `MANAGER`를 최소 분기한다.
- `frontend/src/app/admin/settings/page.tsx`: 사업자 설정, 약관 설정, 정책 설정, 관리자 작업 이력, 직원 관리, 권한 관리 진입점.
- `frontend/src/app/admin/settings/audit-logs/page.tsx`: `/api/admin/audit-logs` 필터 조회와 `/api/admin/audit-logs/{auditLogId}` 상세 조회를 사용해 actionType, 작업자, 대상, 기간 필터와 JSON 요약을 표시한다.
- 직원/부서/직급/권한 그룹/역할별 상세 권한 화면은 v0.4.0으로 이관한다.

## v0.3.5.2 Admin Sidebar Accordion Fix

- `AdminSidebarV2`의 open 상태를 `openGroupLabel` 단일 값으로 변경해 한 번에 하나의 그룹만 열린다.
- query string이 있는 메뉴는 `pathname + searchParams`가 모두 일치할 때만 active 처리한다.
- 중복 메뉴를 정리해 `/admin/sales`는 매출/회계 관리에만, 사업자/약관/정책 설정은 시스템 설정에만 배치한다.

## v0.3.6 Product Admin Operations UI

- `frontend/src/app/admin/products/page.tsx`: 카테고리/기존 상태/판매 상태/전시 상태/재고 상태/안전재고 이하/판매 기간 필터를 제공한다.
- 상품 목록 행 체크박스와 현재 페이지 전체 선택을 제공하며, 선택 상품에 대해 판매 상태/전시 상태 대량 변경과 변경 사유 입력을 지원한다.
- DataTable render 전용 컬럼은 `select`, `stockSummary`, `statusBadge`, `actions`처럼 고유 key를 사용한다.
- `frontend/src/app/admin/products/[id]/page.tsx`: 상품 운영 메모 작성/조회와 상태 변경 이력 조회 패널을 제공한다.

## v0.3.7 Commerce Display Frontend

- `frontend/src/app/page.tsx`: CMS 배너 컴포넌트를 유지하고, 메인 카테고리 바로가기를 관리자 네비 카테고리 API 기준으로 표시한다.
- `frontend/src/components/shop/DynamicCategoryNav.tsx`: `/api/categories/navigation` 결과를 상단 네비에 사용하며 실패 시 최소 fallback을 표시한다.
- `frontend/src/components/shop/ShopHeader.tsx`: 사용자 화면 문구를 한국어로 정리하고 관리자 버튼은 `MANAGER`, `ADMIN`, `SUPER_ADMIN`에게만 노출한다.
- `frontend/src/app/products/page.tsx`: URL `category`, `keyword` query와 가격/재고 필터를 공개 상품 API에 연결한다.
- `frontend/src/components/shop/ProductCard.tsx`: 브랜드, 태그, 정상가/판매가, 할인, 재고 상태, 구매 불가 상태를 표시한다.
- `frontend/src/app/products/[id]/page.tsx`: 브랜드/제조사/원산지/배송 정보, 태그, 상세 블록, 재고 상태와 구매 불가 사유를 표시한다.
- 사용자 화면 타입은 공개 API 필드만 포함하며 `purchasePrice`, `marginRate` 같은 내부 운영 필드는 포함하지 않는다.

## v0.4.2 Staff Management UI

- `frontend/src/app/admin/settings/staff/page.tsx`: 직원 목록, 검색/필터, 직원 등록/수정, 재직 상태 변경, 활성/비활성 변경 화면.
- `frontend/src/lib/services/staffService.ts`: `/api/admin/staff`와 `/api/admin/hr/departments`, `/api/admin/hr/positions` 호출 타입과 API 함수.
- `frontend/src/components/admin/AdminSidebarV2.tsx`: 인사/권한 관리 그룹의 직원 관리 메뉴를 `/admin/settings/staff`로 연결한다.
- 직원 관리 화면은 `SUPER_ADMIN` 메뉴로 노출되며, API 기준으로 조회는 `ADMIN/SUPER_ADMIN`, 변경은 `SUPER_ADMIN` 권한을 따른다.

## v0.4.3 Permission Group UI

- `frontend/src/app/admin/settings/permission-groups/page.tsx`: 권한 그룹 목록, 생성/수정, 활성/비활성, 직원별 권한 그룹 할당 화면.
- `frontend/src/app/admin/settings/roles/page.tsx`: 기존 `USER/MANAGER/ADMIN/SUPER_ADMIN` role과 permission group 병행 운영 정책 안내.
- `frontend/src/lib/services/permissionGroupService.ts`: `/api/admin/permission-groups`와 `/api/admin/users/{userId}/permission-groups` 타입과 API 함수.
- `frontend/src/components/admin/AdminSidebarV2.tsx`: 인사/권한 관리 그룹의 권한 그룹 관리와 역할/권한 설정 메뉴를 실제 페이지 경로로 연결한다.

## v0.4.4 Menu and Feature Permission Matrix UI

- `frontend/src/app/admin/settings/menu-permissions/page.tsx`: 권한 그룹별 기능 권한 체크 매트릭스와 관리자 메뉴별 필요 권한 설정 화면.
- `frontend/src/lib/services/permissionGroupService.ts`: `/api/admin/permissions`, `/api/admin/permission-groups/{groupId}/permissions`, `/api/admin/users/{userId}/permissions`, `/api/admin/menu-permissions` 호출을 포함한다.
- `frontend/src/app/admin/settings/page.tsx`: 설정 홈에 메뉴/기능 권한 카드 추가.
- `frontend/src/components/admin/AdminSidebarV2.tsx`: 인사/권한 관리 그룹에 메뉴/기능 권한 메뉴 추가.
- v0.4.4에서는 매트릭스 관리 UI를 제공하고, 실제 사이드바 permission 기반 노출은 v0.4.5로 이관한다.

## v0.4.5 Admin Sidebar Permission Integration

- `frontend/src/lib/adminMenu.ts`: 관리자 메뉴 그룹, `menuKey`, href, role fallback, active 판정 공용 정의.
- `frontend/src/components/admin/AdminSidebarV2.tsx`: `GET /api/admin/users/me/permissions`와 `GET /api/admin/menu-permissions`를 조회해 `admin_menu_permissions.menu_key`와 effective permission code 기준으로 메뉴를 필터링한다.
- `frontend/src/components/admin/AdminLayout.tsx`: 직접 URL 접근 시 현재 메뉴의 필요 권한을 확인하고 권한이 없으면 403 안내 UX를 표시한다.
- 권한 API 실패 시 기존 role 기반 메뉴 노출과 접근 확인으로 fallback한다.
- 실제 API method 단위 permission 세분화는 v0.4.6으로 이관한다.
