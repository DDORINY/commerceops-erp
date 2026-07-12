# 백엔드 구조 문서

기준 버전: `v0.6.5`
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
│   ├── barcode
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
| accounting | `AdminAccountingController` | `AccountingService` | `AccountingEntryRepository`, `AccountingLedgerRepository`, `AccountingTransactionRepository` | `AccountingEntry`, `AccountingLedger`, `AccountingTransaction` |
| audit | `AdminAuditLogController` | `AuditLogService` | `AuditLogRepository` | `AuditLog` |
| barcode | `AdminBarcodeController` | `BarcodeService` | `BarcodeLabelRepository`, `SkuRepository` | `BarcodeLabel` |
| auth | `AuthController` | `AuthService` | `UserRepository` 사용 | `User` |
| banner | `BannerController`, `AdminBannerController` | `MainBannerService` | `MainBannerRepository` | `MainBanner` |
| cart | `CartController` | `CartService` | `CartRepository` | `Cart` |
| category | `CategoryController`, `AdminCategoryController` | `CategoryService` | `CategoryRepository` | `Category` |
| coupon | `CouponController`, `AdminCouponController` | `CouponService` | `CouponRepository` | `Coupon` |
| dashboard | `AdminDashboardController` | `DashboardService` | 주문/결제/상품/회원 repository 사용 | - |
| hr | `AdminHrController`, `AdminStaffController` | `DepartmentService`, `PositionService`, `StaffProfileService`, `AdminStaffService` | `DepartmentRepository`, `PositionRepository`, `StaffProfileRepository`, `UserRepository` | `Department`, `Position`, `StaffProfile` |
| inquiry | `InquiryController`, `AdminInquiryController` | `InquiryService` | `InquiryRepository` | `Inquiry` |
| inventory | `AdminInventoryController`, `AdminStockCountController`, `AdminInventoryAlertController` | `InventoryService`, `StockCountService`, `InventoryAlertService` | `InventoryLogRepository`, `StockCountSessionRepository`, `StockCountItemRepository`, `InventoryAlertRuleRepository` | `InventoryLog`, `StockCountSession`, `StockCountItem`, `InventoryAlertRule` |
| media | `AdminMediaController` | `MediaStorageService` | `MediaFileRepository` | `MediaFile` |
| notification | `NotificationController`, `AdminNotificationController` | `NotificationService` | `NotificationRepository` | `Notification` |
| ops | `AdminOpsAnalyticsController` | `OpsAnalyticsService` | 회계/주문/결제/창고 repository 사용 | - |
| order | `OrderController`, `AdminOrderController` | `OrderService`, `OrderCancellationService` | `OrderRepository`, `OrderItemRepository` | `Order`, `OrderItem` |
| payment | `PaymentController` | `PaymentService` | `PaymentRepository` | `Payment` |
| permission | `AdminPermissionGroupController`, `AdminPermissionMatrixController` | `PermissionGroupService`, `PermissionMatrixService` | `PermissionGroupRepository`, `UserPermissionGroupRepository`, `PermissionRepository`, `PermissionGroupPermissionRepository`, `AdminMenuPermissionRepository`, `UserRepository` | `PermissionGroup`, `UserPermissionGroup`, `Permission`, `PermissionGroupPermission`, `AdminMenuPermission` |
| product | `ProductController`, `AdminProductController` | `ProductService`, `ProductDetailBlockService` | `ProductRepository`, `ProductDetailBlockRepository`, `ProductStatusHistoryRepository`, `ProductOperationNoteRepository` | `Product`, `ProductDetailBlock`, `ProductStatusHistory`, `ProductOperationNote` |
| production | `AdminProductionController` | `ProductionService` | `ProductionOrderRepository`, `ProductionOrderItemRepository`, `ProductionReceiptRepository` | `ProductionOrder`, `ProductionOrderItem`, `ProductionReceipt` |
| returns | `ReturnController`, `AdminReturnController` | `ReturnService` | `ReturnRequestRepository` | `ReturnRequest` |
| review | `ReviewController`, `AdminReviewController` | `ReviewService` | `ReviewRepository` | `Review` |
| settings | `SettingsController`, `AdminSettingsController` | `SettingsService` | `BusinessSettingsRepository`, `TermsVersionRepository` | `BusinessSettings`, `TermsVersion` |
| shipment | `ShipmentController`, `AdminShipmentController` | `ShipmentService` | `ShipmentRepository` | `Shipment` |
| sku | `AdminSkuController` | `SkuService` | `SkuRepository` | `Sku` |
| user | `AdminUserController` | `AdminUserService` | `UserRepository` | `User` |
| warehouse | `AdminWarehouseController`, `AdminWarehouseLocationController` | `WarehouseService`, `WarehouseLocationService`, `WarehouseFulfillmentService` | `WarehouseRepository`, `WarehouseStockRepository`, `WarehouseLocationRepository`, `WarehouseLocationStockRepository`, `StockReservationRepository`, `StockTransferRepository` | `Warehouse`, `WarehouseStock`, `WarehouseLocation`, `WarehouseLocationStock`, `StockReservation`, `StockTransfer` |
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
- 생산 입고 흐름: v0.5.2 기준 `ProductionService`가 생산 주문 생성/수정/시작/취소/완료를 관리한다. 완료 처리 시 SKU의 상품 재고와 창고 재고를 증가시키고 `ProductionReceipt`, `InventoryLog(PRODUCTION_RECEIPT)`, audit log를 함께 생성한다.
- 바코드 라벨/입출고: v0.5.4 기준 `BarcodeService`가 바코드/SKU 검색, 바코드 단건 조회, 라벨 HTML 미리보기 생성, 출력 이력 기록, 바코드 기반 재고 조회/입고/출고를 제공한다. 조회는 `INVENTORY_READ`, 라벨 생성/출력 기록은 `BARCODE_MANAGE`, 입고/출고는 `INVENTORY_WRITE` permission을 요구하며 `BARCODE_LABEL_CREATED`, `BARCODE_LABEL_PRINTED`, `STOCK_INBOUNDED`, `STOCK_OUTBOUNDED` audit log를 남긴다.
- 재고 실사: v0.5.5 기준 `StockCountService`가 창고별 실사 세션 생성, SKU 품목 저장, 시작/완료/취소를 제공한다. 완료 시 실사 차이를 상품/창고 재고와 `InventoryLog(ADJUST)`에 반영하고 `STOCK_COUNT_*` audit log를 남긴다.
- 창고 위치: v0.5.6 기준 `WarehouseLocationService`가 창고 위치 목록/생성/수정/활성 상태 변경과 위치별 SKU 재고 조회를 제공한다. 위치별 수량 변경과 위치 간 이동은 v0.5.7 재고 이동 고도화로 이관한다.
- 안전재고 알림: v0.5.7 기준 `InventoryAlertService`가 SKU 또는 SKU+창고 단위 안전재고 기준을 관리하고 기준 이하 재고 항목을 조회한다. 조회는 `INVENTORY_READ`, 기준 생성/수정/활성 변경은 `INVENTORY_WRITE` permission을 요구한다.
- 출고 지시: v0.6.1 기준 `OutboundOrderService`가 주문 기준 출고 지시 생성, 목록/상세 조회, 창고/메모 수정, 피킹 완료, 취소를 제공한다. 조회는 `OUTBOUND_READ`, 변경은 `OUTBOUND_MANAGE` permission을 요구하고 출고 생성/수정/피킹/취소는 audit log에 기록한다.
- 출고 바코드 검수: v0.6.7 기준 `OutboundOrderService`가 SKU 바코드 기준 출고 품목 검수를 처리하고 `outbound_scan_logs`에 스캔 이력을 저장한다. 검수는 `OUTBOUND_MANAGE` permission을 요구하며 모든 품목이 검수되면 `PICKED` 상태로 전환된다.
- 택배사/배송 방법: v0.6.2 기준 `ShippingSettingService`가 택배사와 배송 방법 목록/생성/수정/활성 상태 변경을 제공한다. 모든 API는 `CARRIER_MANAGE` permission을 요구하고 생성/수정/활성 상태 변경은 audit log에 기록한다.
- 송장번호/라벨/추적 관리: v0.6.5 기준 `ShipmentService`가 송장번호 수동 저장/수정, 내부 테스트용 자동 생성, 송장 라벨 HTML 미리보기 생성, 출력 이력 기록, 배송 상태 변경, 배송 추적 이벤트 조회/등록을 제공한다. 배송 조회는 `SHIPMENT_READ`, 송장번호 생성/수정/배송완료/상태 변경/추적 이벤트 등록은 `SHIPMENT_MANAGE`, 라벨 생성/출력은 `SHIPPING_LABEL_PRINT` permission을 요구한다.
- 반품 배송 관리: v0.6.6 기준 `ReturnService`가 반품 요청별 수거 택배사, 수거 송장번호, 수거 상태, 반품 배송비, 배송비 부담 주체를 조회/저장한다. 조회는 `ORDER_READ`, 저장은 `RETURN_SHIPPING_MANAGE` permission을 요구하고 변경 내역은 audit log에 기록한다.

## 환경 프로파일

- `application.yml`: 공통 설정. 기본 active profile은 `local`.
- `application-local.yml`: 로컬 DB/JWT 설정. 커밋 제외 대상.
- `application-prod.yml`: 운영 profile 예시. DB/JWT secret은 환경 변수로 주입하고 Hibernate `ddl-auto=validate`를 사용한다.
- `application-test.yml`: 테스트 profile. H2 `create-drop`, Flyway 비활성화.

## v0.1.1 ~ v0.1.6 반영 상태

- 관리자 문의: `GET /api/admin/inquiries`, 답변, 종료 API 실제 연결.
- 관리자 리뷰: `GET /api/admin/reviews`, 숨김/해제/삭제 API 실제 연결. 삭제는 `ReviewStatus.DELETED` soft delete로 처리한다.
- 관리자 회계: `GET /api/admin/accounting/summary`, `GET /api/admin/accounting/entries` 실제 연결. v0.7.1 기준 `GET /api/admin/accounting/ledgers`, `GET /api/admin/accounting/transactions` 원장/거래 조회 기반을 추가했다. v0.7.2 기준 결제 완료 시 주문 매출 `SALES` 거래를 중복 방지로 생성하고, 주문별 매출 인식 조회/실행 API를 제공한다.
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

## v0.7.3 환불/반품 배송비 회계 처리

- `AccountingService`는 결제 환불, 반품 승인 환불, 반품 배송비를 `accounting_transactions`에 반영한다.
- `PaymentService`와 `OrderCancellationService`는 결제 취소/환불 시 환불 회계 거래 생성을 호출한다.
- `ReturnService`는 반품 승인과 반품 배송 정보 저장 시 환불/반품 배송비 회계 거래 생성을 호출한다.
- `AdminAccountingController`는 환불/반품 배송비 수동 반영과 이벤트 목록 조회 API를 제공한다.
- `RETURN_FEE_MANAGE` 권한은 반품 배송비 회계 처리 실행 권한으로 사용한다.

## v0.7.4 택배비 매입/배송비 정산 처리

- `ShippingCostEntry`는 배송 건별 내부 택배비 매입 비용과 고객 청구 배송비 후보를 저장한다.
- `AccountingService`는 배송 건의 `SHIPPING_COST` 회계 거래를 생성한다.
- `ShipmentService`는 운송장 입력, 운송장 자동 생성, 배송 상태 변경 시 택배비 회계 반영을 시도한다.
- `AdminAccountingController`는 배송비 수동 반영, 배송비 항목 조회, 배송비 거래 목록 API를 제공한다.
- `SHIPPING_COST_MANAGE` 권한은 택배비 비용 회계 반영 실행 권한으로 사용한다.

## v0.7.5 정산 배치/마감 처리

- `SettlementBatchService`는 기간별 회계 거래를 조회해 정산 배치와 항목을 생성한다.
- `SettlementBatch`는 `DRAFT`, `CONFIRMED`, `CLOSED`, `CANCELLED` 상태를 가진다.
- `closeBatch`는 중복 마감을 막고 `closedAt`, `closedBy`를 기록한다.
- `AdminAccountingController`는 정산 목록/상세/생성/마감 API를 제공한다.
- `SETTLEMENT_MANAGE`는 정산 생성, `ACCOUNTING_CLOSE`는 정산 마감 권한으로 사용한다.
## v0.8.1 AI 데이터셋 추출 도메인

- `domain/ai/controller/AdminAiDatasetController.java`
  - AI 데이터셋 카탈로그와 샘플 export API를 제공한다.
- `domain/ai/service/AiDatasetExportService.java`
  - 상품, 주문, 리뷰, 회계 거래 데이터를 학습 후보 rows로 변환한다.
- `domain/ai/dto/*`
  - 데이터셋 카탈로그와 export 응답 DTO를 정의한다.
- `domain/ai/enums/AiDatasetKey.java`
  - 지원 데이터셋 key를 관리한다.
