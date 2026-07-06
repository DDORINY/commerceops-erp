# 백엔드 구조 문서

기준 버전: `v0.4.8`
기준 코드: `backend/src/main/java/com/commerceops/erp`

## 기술 스택

- Spring Boot 3.5.0
- Java 17
- Spring Security + JWT
- Spring Data JPA
- Flyway DB Migration
- MySQL 운영 기준, 테스트는 H2 프로파일 사용

## 패키지 구조

```text
com.commerceops.erp
├── CommerceOpsErpApplication.java
├── domain
│   ├── accounting
│   ├── auth
│   ├── audit
│   ├── banner
│   ├── cart
│   ├── category
│   ├── coupon
│   ├── dashboard
│   ├── inquiry
│   ├── inventory
│   ├── media
│   ├── notification
│   ├── ops
│   ├── order
│   ├── payment
│   ├── permission
│   ├── product
│   ├── returns
│   ├── review
│   ├── settings
│   ├── shipment
│   ├── user
│   ├── warehouse
│   └── wishlist
└── global
    ├── config
    ├── exception
    ├── health
    ├── response
    └── security
```

## 도메인별 실제 구성

| 도메인 | Controller | Service | Repository | Entity |
| --- | --- | --- | --- | --- |
| accounting | `AdminAccountingController` | `AccountingService` | `AccountingEntryRepository` | `AccountingEntry` |
| audit | `AdminAuditLogController` | `AuditLogService` | `AuditLogRepository` | `AuditLog` |
| auth | `AuthController` | `AuthService` | `UserRepository` 사용 | `User` |
| banner | `BannerController`, `AdminBannerController` | `MainBannerService` | `MainBannerRepository` | `MainBanner` |
| cart | `CartController` | `CartService` | `CartRepository` | `Cart` |
| category | `CategoryController`, `AdminCategoryController` | `CategoryService` | `CategoryRepository` | `Category` |
| coupon | `CouponController`, `AdminCouponController` | `CouponService` | `CouponRepository` | `Coupon` |
| dashboard | `AdminDashboardController` | `DashboardService` | 주문/결제/상품/회원 repository 사용 | - |
| hr | `AdminHrController`, `AdminStaffController` | `DepartmentService`, `PositionService`, `StaffProfileService`, `AdminStaffService` | `DepartmentRepository`, `PositionRepository`, `StaffProfileRepository`, `UserRepository` | `Department`, `Position`, `StaffProfile` |
| inquiry | `InquiryController`, `AdminInquiryController` | `InquiryService` | `InquiryRepository` | `Inquiry` |
| inventory | `AdminInventoryController` | `InventoryService` | `InventoryLogRepository` | `InventoryLog` |
| media | `AdminMediaController` | `MediaStorageService` | `MediaFileRepository` | `MediaFile` |
| notification | `NotificationController`, `AdminNotificationController` | `NotificationService` | `NotificationRepository` | `Notification` |
| ops | `AdminOpsAnalyticsController` | `OpsAnalyticsService` | 회계/주문/결제/창고 repository 사용 | - |
| order | `OrderController`, `AdminOrderController` | `OrderService`, `OrderCancellationService` | `OrderRepository`, `OrderItemRepository` | `Order`, `OrderItem` |
| payment | `PaymentController` | `PaymentService` | `PaymentRepository` | `Payment` |
| permission | `AdminPermissionGroupController`, `AdminPermissionMatrixController` | `PermissionGroupService`, `PermissionMatrixService` | `PermissionGroupRepository`, `UserPermissionGroupRepository`, `PermissionRepository`, `PermissionGroupPermissionRepository`, `AdminMenuPermissionRepository`, `UserRepository` | `PermissionGroup`, `UserPermissionGroup`, `Permission`, `PermissionGroupPermission`, `AdminMenuPermission` |
| product | `ProductController`, `AdminProductController` | `ProductService`, `ProductDetailBlockService` | `ProductRepository`, `ProductDetailBlockRepository`, `ProductStatusHistoryRepository`, `ProductOperationNoteRepository` | `Product`, `ProductDetailBlock`, `ProductStatusHistory`, `ProductOperationNote` |
| returns | `ReturnController`, `AdminReturnController` | `ReturnService` | `ReturnRequestRepository` | `ReturnRequest` |
| review | `ReviewController`, `AdminReviewController` | `ReviewService` | `ReviewRepository` | `Review` |
| settings | `SettingsController`, `AdminSettingsController` | `SettingsService` | `BusinessSettingsRepository`, `TermsVersionRepository` | `BusinessSettings`, `TermsVersion` |
| shipment | `ShipmentController`, `AdminShipmentController` | `ShipmentService` | `ShipmentRepository` | `Shipment` |
| sku | `AdminSkuController` | `SkuService` | `SkuRepository` | `Sku` |
| user | `AdminUserController` | `AdminUserService` | `UserRepository` | `User` |
| warehouse | `AdminWarehouseController` | `WarehouseService`, `WarehouseFulfillmentService` | `WarehouseRepository`, `WarehouseStockRepository`, `StockReservationRepository`, `StockTransferRepository` | `Warehouse`, `WarehouseStock`, `StockReservation`, `StockTransfer` |
| wishlist | `WishlistController` | `WishlistService` | `WishlistRepository` | `Wishlist` |

## 글로벌 구성

- `SecurityConfig`: JWT stateless 인증, 공개/관리자 권한 라우팅 설정.
- `JwtAuthenticationFilter`: Bearer access token 파싱 후 SecurityContext 설정. refresh token은 인증 토큰으로 사용하지 않는다.
- `CustomUserDetailsService`: 사용자 로딩.
- `JwtTokenProvider`: access token, refresh token 생성/검증.
- `ApiResponse<T>`: 성공 응답 래퍼.
- `PageResponse<T>`: 프론트와 맞춘 페이지 응답 DTO.
- `GlobalExceptionHandler`, `BusinessException`, `ErrorCode`: 공통 예외 처리.
- `HealthController`: `GET /api/health`.
- `MediaWebConfig`: `COMMERCEOPS_MEDIA_UPLOAD_DIR` 기준 로컬 업로드 디렉터리를 `/uploads/**` 정적 리소스로 제공.
- Flyway: `backend/src/main/resources/db/migration`의 SQL을 운영/로컬 스키마 기준으로 사용한다. 테스트 프로파일은 기존 H2 `create-drop` 회귀 테스트를 유지하기 위해 Flyway를 비활성화한다.
- `CorsConfig`: `COMMERCEOPS_CORS_ALLOWED_ORIGINS` 또는 `commerceops.cors.allowed-origins` 설정으로 허용 origin을 분리한다.
- 관리자 권한: `MANAGER`는 운영 조회 중심 GET API에 접근하고, 데이터 변경/권한/회계/쿠폰/리뷰 운영/감사 로그는 `ADMIN`, `SUPER_ADMIN`으로 제한한다.
- HR 기본 조회 API: v0.4.1 기준 `/api/admin/hr/**`는 기존 role 기반 정책을 유지하며 `ADMIN`, `SUPER_ADMIN`만 접근한다. permission group 기반 접근 제어는 v0.4.3 이후로 이관한다.
- 운영 분석: `OpsAnalyticsService`는 신규 테이블 없이 `AccountingEntry`, `Order`, `Payment`, `Warehouse`, `WarehouseStock`, `StockReservation` 데이터를 읽기 전용으로 집계한다.
- 상품 마스터: v0.3.1 기준 `Product`는 상품코드, 브랜드, 제조사, 모델명, 원산지, 정상가/할인금액/매입가, 검색 키워드, 태그, 판매 기간, 배송/SEO 필드를 포함한다. 사용자 응답은 원가/마진을 제외하고, 관리자 응답은 `AdminProductResponse` 계열 DTO로 내부 운영 필드를 포함한다.
- 상품 상세 CMS: v0.3.2 기준 `ProductDetailBlock`은 상품별 상세 블록을 `HEADING`, `TEXT`, `IMAGE`, `NOTICE`, `SPEC_TABLE`, `HTML` 타입으로 저장한다. 관리자 API는 전체 블록을 조회/교체 저장하고, 사용자 상품 상세 응답은 visible 블록만 sortOrder 순서로 포함한다.
- 카테고리 네비: v0.3.3 기준 `Category`는 parent/depth/sortOrder/active/visibleInNav/slug를 포함한다. 공개 네비 API는 active=true, visibleInNav=true 카테고리만 트리로 반환하고, 관리자 API는 전체 트리를 조회/생성/수정한다.
- 메인 배너 CMS: v0.3.4 기준 `MainBanner`는 title/subtitle/description/imageUrl/linkUrl/position/sortOrder/active/startsAt/endsAt를 포함한다. 공개 API는 활성 상태와 노출 기간 기준 배너만 반환하고, 관리자 API는 전체 배너 조회/등록/수정/비활성화를 제공한다.
- 상품 운영 UX: v0.3.6 기준 관리자 상품 목록은 카테고리/재고/판매 기간 필터를 지원한다. `PATCH /api/admin/products/bulk-status`는 선택 상품의 판매/전시 상태를 일괄 변경하며, 실제 상태 변경은 `ProductStatusHistory`에 기록한다. 운영 메모는 `ProductOperationNote`에 누적 기록하고 상태 변경/대량 변경/메모 작성은 `AuditLog`에도 요약 저장한다.
- HR 조직 기반: v0.4.1 기준 `Department`, `Position`, `StaffProfile`을 추가했다. 직원 프로필은 `User`와 1:1로 연결되며, 부서/직급은 nullable로 시작한다. 생성/수정 API와 관리자 화면은 v0.4.2 이후 범위다.
- 직원 관리: v0.4.2 기준 `AdminStaffController`가 `/api/admin/staff` 목록/상세/등록/수정/재직 상태/활성 상태 변경 API를 제공한다. 조회는 `ADMIN`, `SUPER_ADMIN`, 변경은 `SUPER_ADMIN` 기준으로 제한한다. 직원 생성/수정/상태 변경/활성 변경은 `AuditLog`에 기록한다.
- 권한 그룹 관리: v0.4.3 기준 `AdminPermissionGroupController`가 `/api/admin/permission-groups` 목록/상세/생성/수정/활성 상태 변경 API와 `/api/admin/users/{userId}/permission-groups` 조회/할당 API를 제공한다. 기존 role 기반 접근 제어는 유지하고, 조회는 `ADMIN`, `SUPER_ADMIN`, 변경은 `SUPER_ADMIN`으로 제한한다. 권한 그룹 작업은 `AuditLog`에 기록한다.
- 메뉴/기능 권한 매트릭스: v0.4.5 기준 `AdminPermissionMatrixController`가 `/api/admin/permissions`, `/api/admin/permission-groups/{groupId}/permissions`, `/api/admin/users/{userId}/permissions`, `/api/admin/users/me/permissions`, `/api/admin/menu-permissions`를 제공한다. 현재 사용자 권한과 메뉴 권한 조회는 관리자 사이드바 연동을 위해 `MANAGER`도 접근 가능하며, 변경은 `SUPER_ADMIN`으로 제한한다. `SUPER_ADMIN`은 모든 활성 권한, 그 외 관리자는 사용자 권한 그룹 또는 role 기본 시스템 그룹 기준으로 유효 권한을 계산한다.
- API permission 정책: v0.4.6 기준 `PermissionChecker`가 Controller method 시작부에서 현재 사용자의 effective permission code를 검증한다. `SecurityConfig`는 `/api/admin/**`에 대한 role 기반 1차 접근을 유지하고, 상품/주문/결제/재고/창고/회계/쿠폰/리뷰/문의/직원/권한/감사 로그 등 주요 관리자 API는 permission code로 세부 실행 권한을 나눈다. 권한 없음은 403과 한국어 안내 메시지로 반환한다.
- 감사 로그 확장: v0.4.7 기준 `AuditLogService`는 관리자 변경 작업의 action/target/status 요약과 함께 요청 IP, User-Agent, method/path, before/after/metadata JSON을 기록한다. `AdminAuditLogController`는 actor/action/target/date 필터와 상세 조회를 제공하며, `PermissionChecker`는 인증된 관리자의 permission code 부족으로 인한 403을 `PERMISSION_DENIED`로 남긴다.
- 사업자/약관 설정: v0.4.8 기준 `SettingsService`가 단일 row 사업자 설정과 약관/개인정보처리방침/배송반품정책 버전 이력을 관리한다. 관리자 API는 `SETTINGS_MANAGE` permission을 요구하고 공개 API는 인증 없이 최신 공개 정보만 조회한다.
- SKU/바코드 재고 마스터: v0.5.1 기준 `SkuService`가 상품별 SKU 코드와 바코드를 관리한다. 조회는 `INVENTORY_READ`, 생성/수정/활성 변경은 `SKU_MANAGE`, 바코드 재발급은 `BARCODE_MANAGE` permission을 요구하며 주요 변경은 audit log에 기록한다.

## 환경 프로파일

- `application.yml`: 공통 설정. 기본 active profile은 `local`.
- `application-local.yml`: 로컬 DB/JWT 설정. 커밋 제외 대상.
- `application-prod.yml`: 운영 profile 예시. DB/JWT secret은 환경 변수로 주입하고 Hibernate `ddl-auto=validate`를 사용한다.
- `application-test.yml`: 테스트 profile. H2 `create-drop`, Flyway 비활성화.

## v0.1.1 ~ v0.1.6 반영 상태

- 관리자 문의: `GET /api/admin/inquiries`, 답변, 종료 API 실제 연결.
- 관리자 리뷰: `GET /api/admin/reviews`, 숨김/해제/삭제 API 실제 연결. 삭제는 `ReviewStatus.DELETED` soft delete로 처리한다.
- 관리자 회계: `GET /api/admin/accounting/summary`, `GET /api/admin/accounting/entries` 실제 연결.
- 관리자 판매/매출 분석: `GET /api/admin/dashboard/summary`, `/sales`, `/top-products` 사용.
- 관리자 창고: 창고 목록/등록, 창고별 재고, 재고 할당, 재고 이동 실제 API 구현.
- 사용자 쇼핑 화면: mock 파일 제거, 상품/카테고리/장바구니/주문/위시리스트/문의/리뷰 service 기반 연결.

## 명시적 미구현

- 실제 PG 벤더 키/웹훅/리다이렉트 연동. 현재 `PaymentController`는 `/api/payments/approve`, `/api/payments/{paymentId}/cancel`, 하위 호환 `/api/payments/mock/complete`를 제공하며 `MOCK_PROVIDER` 기반으로 동작한다.
- 감사 로그 CSV 다운로드, 장기 보관/아카이빙, 외부 SIEM 연동. v0.4.7 기준 주요 관리자 변경 작업과 인증된 관리자의 권한 실패는 `audit_logs`에 기록한다.
- S3/CDN, 이미지 리사이징, 썸네일, 다중 이미지 갤러리.
- 고급 WMS 피킹/패킹/출고 자동화.
- 복식부기 기반 정산/마감 리포트.
- 대규모 BI/데이터 웨어하우스와 WMS 자동 피킹/패킹 최적화. v0.2.8에서는 기초 overview API와 문서 기준만 추가했다.

## v0.3.5 Product Sales Status

- `domain/product/enums/ProductSalesStatus.java`: 판매 운영 상태(`DRAFT`, `ON_SALE`, `PAUSED`, `SOLD_OUT`, `DISCONTINUED`).
- `domain/product/enums/ProductDisplayStatus.java`: 전시 상태(`VISIBLE`, `HIDDEN`).
- `domain/product/enums/StockDisplayStatus.java`: 사용자 재고 표시 상태(`IN_STOCK`, `LOW_STOCK`, `SOLD_OUT`).
- `Product.isPurchasable()` 기준을 장바구니와 주문 생성 검증에서 함께 사용한다.
- 관리자 상품 상태 부분 변경 API는 `PATCH /api/admin/products/{productId}/status`이다.

## v0.3.6 Product Admin Operations

- `domain/product/entity/ProductStatusHistory.java`: 상품 판매/전시 상태 변경 전후와 작업자 snapshot을 저장한다.
- `domain/product/entity/ProductOperationNote.java`: 상품별 관리자 운영 메모를 저장한다.
- `PATCH /api/admin/products/bulk-status`: 선택 상품의 판매 상태와 전시 상태를 전체 성공/전체 실패 방식으로 일괄 변경한다.
- `GET /api/admin/products/{productId}/status-history`: 최근 상태 변경 이력을 조회한다.
- `GET /api/admin/products/{productId}/operation-notes`, `POST /api/admin/products/{productId}/operation-notes`: 상품 운영 메모 조회/작성.
